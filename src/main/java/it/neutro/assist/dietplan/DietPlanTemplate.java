package it.neutro.assist.dietplan;

import it.neutro.assist.assessment.GoalType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diet_plan_templates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DietPlanTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoalType goal;

    @Column(nullable = false)
    private int totalDays;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateDay> days = new ArrayList<>();

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
