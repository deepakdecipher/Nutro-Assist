package it.neutro.assist.dietplan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {

    List<DietPlan> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT p FROM DietPlan p WHERE p.userId = :userId AND p.planStatus = 'ACTIVE'")
    Optional<DietPlan> findActivePlan(@Param("userId") Long userId);
}
