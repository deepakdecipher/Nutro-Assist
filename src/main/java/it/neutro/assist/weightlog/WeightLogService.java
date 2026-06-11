package it.neutro.assist.weightlog;

import it.neutro.assist.shared.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeightLogService {

    private final WeightLogRepository weightLogRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public WeightLogResponse add(WeightLogRequest req) {
        Long userId = currentUserService.getCurrentUserId();
        WeightLog log = WeightLog.builder()
                .userId(userId)
                .weightKg(req.weightKg())
                .logDate(req.logDate())
                .notes(req.notes())
                .build();
        return WeightLogResponse.from(weightLogRepository.save(log));
    }

    public List<WeightLogResponse> getAll() {
        Long userId = currentUserService.getCurrentUserId();
        return weightLogRepository.findByUserIdOrderByLogDateDesc(userId)
                .stream().map(WeightLogResponse::from).toList();
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        WeightLog log = weightLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Weight log not found"));
        if (!log.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Weight log not found");
        }
        weightLogRepository.delete(log);
    }
}
