package it.neutro.assist.dietplan;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_days",
       uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "plan_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private DietPlan plan;

    @Column(nullable = false)
    private int dayNumber;

    @Column(nullable = false)
    private LocalDate planDate;

    @OneToMany(mappedBy = "planDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanMeal> meals = new ArrayList<>();
}
