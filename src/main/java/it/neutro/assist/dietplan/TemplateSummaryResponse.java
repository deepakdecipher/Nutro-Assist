package it.neutro.assist.dietplan;

import it.neutro.assist.assessment.GoalType;

import java.time.LocalDateTime;

public record TemplateSummaryResponse(Long id, String name, GoalType goal, int totalDays, LocalDateTime createdAt) {}
