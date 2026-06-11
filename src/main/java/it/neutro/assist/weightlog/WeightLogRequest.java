package it.neutro.assist.weightlog;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WeightLogRequest(
        @DecimalMin("10.0") @DecimalMax("500.0") double weightKg,
        @NotNull LocalDate logDate,
        String notes
) {}
