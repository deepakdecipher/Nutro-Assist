package it.neutro.assist.dietplan;

public record PlanMealResponse(
        Long planMealId,
        MealType mealType,
        String mealName,
        String description,
        int plannedCalories,
        double proteinG,
        double carbsG,
        double fatG,
        boolean isLogged,
        int consumedCalories
) {}
