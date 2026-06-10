package it.neutro.assist.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> summary() {
        return ResponseEntity.ok(adminDashboardService.summary());
    }

    @GetMapping("/users")
    public ResponseEntity<PageResponse<AdminUserResponse>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(adminDashboardService.users(UserFilter.ALL, pageable(page, size), search, sort));
    }

    @GetMapping("/users/admins")
    public ResponseEntity<PageResponse<AdminUserResponse>> admins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(adminDashboardService.users(UserFilter.ADMINS, pageable(page, size), search, sort));
    }

    @GetMapping("/users/regular")
    public ResponseEntity<PageResponse<AdminUserResponse>> regularUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(adminDashboardService.users(UserFilter.REGULAR, pageable(page, size), search, sort));
    }

    private Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        return PageRequest.of(safePage, safeSize);
    }
}
