package it.neutro.assist.dietplan;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_meals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_day_id", nullable = false)
    private PlanDay planDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealType mealType;

    @Column(nullable = false, length = 200)
    private String mealName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int calories;

    @Column
    private double proteinG;

    @Column
    private double carbsG;

    @Column
    private double fatG;

    @Column(nullable = false)
    private int displayOrder;
}
