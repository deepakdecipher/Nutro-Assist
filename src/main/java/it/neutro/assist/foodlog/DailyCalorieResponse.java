package it.neutro.assist.foodlog;

import java.time.LocalDate;
import java.util.List;

public record DailyCalorieResponse(
        LocalDate date,
        int dailyCalorieTarget,
        int totalConsumed,
        int remaining,
        List<FoodLogResponse> logs
) {}
