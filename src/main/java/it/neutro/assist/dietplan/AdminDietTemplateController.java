package it.neutro.assist.dietplan;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/diet-templates")
@PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminDietTemplateController {

    private final DietPlanService dietPlanService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<TemplateSummaryResponse> upload(
            @RequestParam String name,
            @RequestParam String goal,
            @RequestParam("file") MultipartFile file) {
        DietPlanTemplate template = dietPlanService.uploadTemplate(name, goal, file);
        return ResponseEntity.ok(new TemplateSummaryResponse(
                template.getId(), template.getName(), template.getGoal(),
                template.getTotalDays(), template.getCreatedAt()));
    }

    @GetMapping
    public ResponseEntity<List<TemplateSummaryResponse>> list() {
        return ResponseEntity.ok(dietPlanService.listTemplates());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dietPlanService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
