package it.neutro.assist.foodlog;

import jakarta.validation.constraints.NotNull;

public record FoodLogRequest(
        @NotNull Long planDayId,
        @NotNull Long planMealId
) {}
