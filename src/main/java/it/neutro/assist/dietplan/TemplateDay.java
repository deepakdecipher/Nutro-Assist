package it.neutro.assist.dietplan;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "template_days",
       uniqueConstraints = @UniqueConstraint(columnNames = {"template_id", "day_number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TemplateDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private DietPlanTemplate template;

    @Column(nullable = false)
    private int dayNumber;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateMeal> meals = new ArrayList<>();
}
