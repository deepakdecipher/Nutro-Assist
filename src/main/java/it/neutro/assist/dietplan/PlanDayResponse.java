package it.neutro.assist.dietplan;

import java.time.LocalDate;
import java.util.List;

public record PlanDayResponse(
        Long planDayId,
        LocalDate date,
        int dayNumber,
        String dayLabel,
        boolean isToday,
        int totalPlannedCalories,
        int totalConsumedCalories,
        List<PlanMealResponse> meals
) {}
