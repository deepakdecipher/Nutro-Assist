package it.neutro.assist.foodlog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/food-logs")
@RequiredArgsConstructor
public class FoodLogController {

    private final FoodLogService foodLogService;

    @PostMapping
    public ResponseEntity<FoodLogResponse> log(@Valid @RequestBody FoodLogRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodLogService.log(req));
    }

    @GetMapping
    public ResponseEntity<DailyCalorieResponse> getDailyTracking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(foodLogService.getDailyTracking(date != null ? date : LocalDate.now()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
