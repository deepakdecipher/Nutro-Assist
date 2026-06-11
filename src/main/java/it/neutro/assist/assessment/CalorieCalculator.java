package it.neutro.assist.assessment;

/**
 * Harris-Benedict BMR → TDEE calculation.
 */
public final class CalorieCalculator {

    private CalorieCalculator() {}

    public static int calculateDailyTarget(AssessmentRequest req) {
        double bmr = "female".equalsIgnoreCase(req.gender())
                ? 655.1 + (9.563 * req.weightKg()) + (1.850 * req.heightCm()) - (4.676 * req.age())
                : 66.47 + (13.75 * req.weightKg()) + (5.003 * req.heightCm()) - (6.755 * req.age());

        double tdee = bmr * activityMultiplier(req.activityLevel());

        return (int) Math.round(applyGoalAdjustment(tdee, req.goal()));
    }

    private static double activityMultiplier(ActivityLevel level) {
        return switch (level) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
            case EXTRA_ACTIVE -> 1.9;
        };
    }

    private static double applyGoalAdjustment(double tdee, GoalType goal) {
        return switch (goal) {
            case WEIGHT_LOSS -> tdee - 500;
            case WEIGHT_GAIN, MUSCLE_BUILDING -> tdee + 300;
            case MAINTAIN_WEIGHT, IMPROVE_FITNESS -> tdee;
        };
    }
}
