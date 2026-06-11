package it.neutro.assist.assessment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping
    public ResponseEntity<AssessmentResponse> saveOrUpdate(@Valid @RequestBody AssessmentRequest req) {
        return ResponseEntity.ok(assessmentService.saveOrUpdate(req));
    }

    @GetMapping("/me")
    public ResponseEntity<AssessmentResponse> getMyAssessment() {
        return ResponseEntity.ok(assessmentService.getMyAssessment());
    }

    @GetMapping("/status")
    public ResponseEntity<AssessmentStatusResponse> getStatus() {
        return ResponseEntity.ok(assessmentService.getStatus());
    }
}
