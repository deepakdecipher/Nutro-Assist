package it.neutro.assist.weightlog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weight-logs")
@RequiredArgsConstructor
public class WeightLogController {

    private final WeightLogService weightLogService;

    @PostMapping
    public ResponseEntity<WeightLogResponse> add(@Valid @RequestBody WeightLogRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(weightLogService.add(req));
    }

    @GetMapping
    public ResponseEntity<List<WeightLogResponse>> getAll() {
        return ResponseEntity.ok(weightLogService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        weightLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
