package it.neutro.assist.foodlog;

import it.neutro.assist.dietplan.MealType;

import java.time.LocalDateTime;

public record FoodLogResponse(
        Long id,
        Long planDayId,
        Long planMealId,
        MealType mealType,
        String foodName,
        String quantityDescription,
        int caloriesConsumed,
        double proteinG,
        double carbsG,
        double fatG,
        LocalDateTime loggedAt
) {
    static FoodLogResponse from(FoodLog f) {
        return new FoodLogResponse(
                f.getId(), f.getPlanDayId(), f.getPlanMealId(),
                f.getMealType(), f.getFoodName(), f.getQuantityDescription(),
                f.getCaloriesConsumed(), f.getProteinG(), f.getCarbsG(), f.getFatG(),
                f.getLoggedAt()
        );
    }
}
