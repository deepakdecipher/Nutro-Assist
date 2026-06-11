package it.neutro.assist.weightlog;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeightLogResponse(Long id, double weightKg, LocalDate logDate, String notes, LocalDateTime createdAt) {

    static WeightLogResponse from(WeightLog w) {
        return new WeightLogResponse(w.getId(), w.getWeightKg(), w.getLogDate(), w.getNotes(), w.getCreatedAt());
    }
}
