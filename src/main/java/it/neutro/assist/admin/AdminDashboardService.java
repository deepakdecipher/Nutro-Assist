package it.neutro.assist.admin;

import com.usermanagement.modelentity.Role;
import com.usermanagement.modelentity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {
    private static final List<String> ADMIN_ROLES = List.of("ADMIN", "SUPER_ADMIN");

    private final EntityManager entityManager;

    public AdminDashboardService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public DashboardSummaryResponse summary() {
        long totalUsers = count(UserFilter.ALL, "");
        long totalAdmins = count(UserFilter.ADMINS, "");
        long totalRegularUsers = count(UserFilter.REGULAR, "");
        long totalActiveUsers = countActiveUsers();
        return new DashboardSummaryResponse(totalUsers, totalAdmins, totalRegularUsers, totalActiveUsers, null, null);
    }

    public PageResponse<AdminUserResponse> users(UserFilter filter, Pageable pageable, String search, String sort) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        root.fetch("roles", JoinType.LEFT);
        cq.distinct(true);
        cq.where(predicates(cb, cq, root, filter, search).toArray(Predicate[]::new));
        cq.orderBy(order(cb, root, sort));

        TypedQuery<User> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<AdminUserResponse> content = query.getResultList().stream()
                .map(this::toResponse)
                .toList();
        long total = count(filter, search);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / pageable.getPageSize());

        return new PageResponse<>(
                content,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                total,
                totalPages
        );
    }

    private long count(UserFilter filter, String search) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);
        cq.select(cb.countDistinct(root));
        cq.where(predicates(cb, cq, root, filter, search).toArray(Predicate[]::new));
        return entityManager.createQuery(cq).getSingleResult();
    }

    private long countActiveUsers() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);
        cq.select(cb.countDistinct(root));
        cq.where(cb.isTrue(root.get("verified")));
        return entityManager.createQuery(cq).getSingleResult();
    }

    private List<Predicate> predicates(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<User> root, UserFilter filter, String search) {
        List<Predicate> predicates = new ArrayList<>();
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        if (!normalizedSearch.isBlank()) {
            String pattern = "%" + normalizedSearch + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("userFullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            ));
        }

        if (filter == UserFilter.ADMINS) {
            Join<User, Role> role = root.join("roles", JoinType.INNER);
            predicates.add(role.get("roleName").in(ADMIN_ROLES));
        }

        if (filter == UserFilter.REGULAR) {
            Subquery<Long> subquery = cq.subquery(Long.class);
            Root<User> subRoot = subquery.from(User.class);
            Join<User, Role> subRole = subRoot.join("roles", JoinType.INNER);
            subquery.select(subRoot.get("id"));
            subquery.where(
                    cb.equal(subRoot.get("id"), root.get("id")),
                    subRole.get("roleName").in(ADMIN_ROLES)
            );
            predicates.add(cb.not(cb.exists(subquery)));
        }

        return predicates;
    }

    private List<Order> order(CriteriaBuilder cb, Root<User> root, String sort) {
        String[] parts = (sort == null ? "createdAt,desc" : sort).split(",", 2);
        String field = parts.length > 0 ? parts[0] : "createdAt";
        boolean asc = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]);

        if ("name".equalsIgnoreCase(field)) {
            return List.of(asc ? cb.asc(cb.lower(root.get("userFullName"))) : cb.desc(cb.lower(root.get("userFullName"))));
        }

        return List.of(asc ? cb.asc(root.get("id")) : cb.desc(root.get("id")));
    }

    private AdminUserResponse toResponse(User user) {
        List<String> roles = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream().map(Role::getRoleName).sorted().toList();

        return new AdminUserResponse(
                user.getId(),
                user.getUserFullName(),
                user.getEmail(),
                roles,
                user.isVerified() ? "ACTIVE" : "PENDING_VERIFICATION",
                null,
                null
        );
    }
}
