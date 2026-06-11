package it.neutro.assist.dietplan;

import it.neutro.assist.assessment.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DietPlanTemplateRepository extends JpaRepository<DietPlanTemplate, Long> {

    List<DietPlanTemplate> findByGoalOrderByCreatedAtDesc(GoalType goal);

    Optional<DietPlanTemplate> findTopByGoalOrderByCreatedAtDesc(GoalType goal);
}
