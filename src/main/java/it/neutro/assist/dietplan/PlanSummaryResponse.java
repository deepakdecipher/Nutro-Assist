package it.neutro.assist.dietplan;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PlanSummaryResponse(
        Long id,
        PlanType planType,
        PlanStatus planStatus,
        int dailyCalorieTarget,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt
) {}
