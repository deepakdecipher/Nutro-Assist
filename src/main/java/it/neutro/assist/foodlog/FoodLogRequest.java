package it.neutro.assist.foodlog;

import it.neutro.assist.dietplan.MealType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FoodLogRequest(
        @NotNull Long planDayId,
        @NotNull Long planMealId,
        @NotNull MealType mealType,
        @NotBlank String foodName,
        String quantityDescription,
        @Min(0) int caloriesConsumed,
        double proteinG,
        double carbsG,
        double fatG
) {}
