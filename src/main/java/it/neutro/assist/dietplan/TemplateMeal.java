package it.neutro.assist.dietplan;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "template_meals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TemplateMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private TemplateDay day;

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
