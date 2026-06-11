package it.neutro.assist.foodlog;

import it.neutro.assist.assessment.UserAssessmentRepository;
import it.neutro.assist.dietplan.PlanDay;
import it.neutro.assist.dietplan.PlanDayRepository;
import it.neutro.assist.dietplan.PlanMeal;
import it.neutro.assist.dietplan.PlanMealRepository;
import it.neutro.assist.shared.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodLogService {

    private final FoodLogRepository foodLogRepository;
    private final PlanDayRepository planDayRepository;
    private final PlanMealRepository planMealRepository;
    private final UserAssessmentRepository assessmentRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public FoodLogResponse log(FoodLogRequest req) {
        Long userId = currentUserService.getCurrentUserId();

        // Validate plan day and meal belong to the user's plan
        PlanDay planDay = planDayRepository.findById(req.planDayId())
                .orElseThrow(() -> new IllegalArgumentException("Plan day not found"));
        if (!planDay.getPlan().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Plan day not found");
        }

        PlanMeal planMeal = planMealRepository.findById(req.planMealId())
                .orElseThrow(() -> new IllegalArgumentException("Plan meal not found"));
        if (!planMeal.getPlanDay().getId().equals(req.planDayId())) {
            throw new IllegalArgumentException("Plan meal does not belong to the specified day");
        }

        // Upsert: if already logged for this meal, update it
        FoodLog log = foodLogRepository.findByUserIdAndPlanMealId(userId, req.planMealId())
                .orElse(FoodLog.builder()
                        .userId(userId)
                        .planDayId(req.planDayId())
                        .planMealId(req.planMealId())
                        .build());

        log.setMealType(req.mealType());
        log.setFoodName(req.foodName());
        log.setQuantityDescription(req.quantityDescription());
        log.setCaloriesConsumed(req.caloriesConsumed());
        log.setProteinG(req.proteinG());
        log.setCarbsG(req.carbsG());
        log.setFatG(req.fatG());

        return FoodLogResponse.from(foodLogRepository.save(log));
    }

    public DailyCalorieResponse getDailyTracking(LocalDate date) {
        Long userId = currentUserService.getCurrentUserId();

        int dailyTarget = assessmentRepository.findByUserId(userId)
                .map(a -> a.getDailyCalorieTarget())
                .orElse(0);

        // Find the plan day for this date owned by the user
        // We need to look up by date across all user's plans
        List<FoodLog> logs = List.of();
        // Find plan days for the user on this date via a query on planDay
        var planDays = planDayRepository.findByUserIdAndPlanDate(userId, date);
        if (!planDays.isEmpty()) {
            logs = foodLogRepository.findByUserIdAndPlanDayId(userId, planDays.get(0).getId());
        }

        int totalConsumed = logs.stream().mapToInt(FoodLog::getCaloriesConsumed).sum();
        List<FoodLogResponse> responses = logs.stream().map(FoodLogResponse::from).toList();

        return new DailyCalorieResponse(date, dailyTarget, totalConsumed,
                Math.max(0, dailyTarget - totalConsumed), responses);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        FoodLog log = foodLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food log not found"));
        if (!log.getUserId().equals(userId)) throw new IllegalArgumentException("Food log not found");
        foodLogRepository.delete(log);
    }
}
