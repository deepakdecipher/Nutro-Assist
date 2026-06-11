package it.neutro.assist.assessment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_assessments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(nullable = false)
    private double heightCm;

    @Column(nullable = false)
    private double weightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoalType goal;

    @Column(columnDefinition = "TEXT")
    private String dietaryPreferences;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String medicalConditions;

    @Column(columnDefinition = "TEXT")
    private String foodInterests;

    @Column(nullable = false)
    private int dailyCalorieTarget;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
