package it.neutro.assist.foodlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findByUserIdAndPlanDayId(Long userId, Long planDayId);

    Optional<FoodLog> findByUserIdAndPlanMealId(Long userId, Long planMealId);

    @Query("SELECT f.planMealId FROM FoodLog f WHERE f.userId = :userId AND f.planDayId IN :planDayIds")
    Set<Long> findLoggedMealIds(@Param("userId") Long userId, @Param("planDayIds") List<Long> planDayIds);
}
