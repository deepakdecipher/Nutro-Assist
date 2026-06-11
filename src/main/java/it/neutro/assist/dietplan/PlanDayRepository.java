package it.neutro.assist.dietplan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlanDayRepository extends JpaRepository<PlanDay, Long> {

    @Query("SELECT d FROM PlanDay d WHERE d.plan.id = :planId AND d.planDate BETWEEN :from AND :to ORDER BY d.planDate")
    List<PlanDay> findWeek(@Param("planId") Long planId,
                           @Param("from") LocalDate from,
                           @Param("to") LocalDate to);

    Optional<PlanDay> findByPlanIdAndPlanDate(Long planId, LocalDate date);

    @Query("SELECT d FROM PlanDay d WHERE d.plan.userId = :userId AND d.planDate = :date")
    List<PlanDay> findByUserIdAndPlanDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
