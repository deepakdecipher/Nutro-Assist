package it.neutro.assist.assessment;

import java.time.LocalDateTime;

public record AssessmentResponse(
        Long id,
        String fullName,
        int age,
        String gender,
        double heightCm,
        double weightKg,
        ActivityLevel activityLevel,
        GoalType goal,
        String dietaryPreferences,
        String allergies,
        String medicalConditions,
        String foodInterests,
        int dailyCalorieTarget,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    static AssessmentResponse from(UserAssessment a) {
        return new AssessmentResponse(
                a.getId(), a.getFullName(), a.getAge(), a.getGender(),
                a.getHeightCm(), a.getWeightKg(), a.getActivityLevel(), a.getGoal(),
                a.getDietaryPreferences(), a.getAllergies(), a.getMedicalConditions(),
                a.getFoodInterests(), a.getDailyCalorieTarget(), a.isCompleted(),
                a.getCreatedAt(), a.getUpdatedAt()
        );
    }
}
