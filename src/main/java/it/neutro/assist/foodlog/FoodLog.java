package it.neutro.assist.foodlog;

import it.neutro.assist.dietplan.MealType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "food_logs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "plan_meal_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_day_id", nullable = false)
    private Long planDayId;

    @Column(name = "plan_meal_id", nullable = false)
    private Long planMealId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealType mealType;

    @Column(nullable = false, length = 200)
    private String foodName;

    @Column(length = 100)
    private String quantityDescription;

    @Column(nullable = false)
    private int caloriesConsumed;

    @Column
    private double proteinG;

    @Column
    private double carbsG;

    @Column
    private double fatG;

    @Column(nullable = false, updatable = false)
    private LocalDateTime loggedAt;

    @PrePersist
    void prePersist() {
        loggedAt = LocalDateTime.now();
    }
}
