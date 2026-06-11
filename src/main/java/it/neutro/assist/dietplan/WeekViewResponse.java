package it.neutro.assist.dietplan;

import java.time.LocalDate;
import java.util.List;

public record WeekViewResponse(
        Long planId,
        PlanType planType,
        PlanStatus planStatus,
        int dailyCalorieTarget,
        LocalDate startDate,
        LocalDate endDate,
        int todayConsumedCalories,
        List<PlanDayResponse> week
) {}
