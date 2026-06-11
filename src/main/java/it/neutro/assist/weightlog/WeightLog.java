package it.neutro.assist.weightlog;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weight_logs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "log_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeightLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private double weightKg;

    @Column(nullable = false)
    private LocalDate logDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
