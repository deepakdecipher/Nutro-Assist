package it.neutro.assist.dietplan;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/diet-plans")
@RequiredArgsConstructor
public class DietPlanController {

    private final DietPlanService dietPlanService;

    @GetMapping("/active")
    public ResponseEntity<WeekViewResponse> getActivePlan() {
        Optional<WeekViewResponse> plan = dietPlanService.getActivePlanWeekView();
        return plan.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<PlanSummaryResponse>> getHistory() {
        return ResponseEntity.ok(dietPlanService.getHistory());
    }

    @PostMapping("/generate")
    public ResponseEntity<WeekViewResponse> generateAiPlan() {
        return ResponseEntity.ok(dietPlanService.generateAiPlan());
    }

    @PostMapping("/assign/{templateId}")
    public ResponseEntity<WeekViewResponse> assignNutritionistPlan(@PathVariable Long templateId) {
        return ResponseEntity.ok(dietPlanService.assignNutritionistPlan(templateId));
    }

    @GetMapping("/{id}/week")
    public ResponseEntity<WeekViewResponse> getWeekView(@PathVariable Long id) {
        return ResponseEntity.ok(dietPlanService.getWeekView(id));
    }

    @GetMapping("/{id}/day/{date}")
    public ResponseEntity<PlanDayResponse> getDayView(
            @PathVariable Long id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(dietPlanService.getDayView(id, date));
    }
}
