package it.neutro.assist.assessment;

import jakarta.validation.constraints.*;

public record AssessmentRequest(
        @NotBlank String fullName,
        @Min(1) @Max(120) int age,
        @NotBlank String gender,
        @DecimalMin("50.0") @DecimalMax("300.0") double heightCm,
        @DecimalMin("10.0") @DecimalMax("500.0") double weightKg,
        @NotNull ActivityLevel activityLevel,
        @NotNull GoalType goal,
        String dietaryPreferences,
        String allergies,
        String medicalConditions,
        String foodInterests
) {}
