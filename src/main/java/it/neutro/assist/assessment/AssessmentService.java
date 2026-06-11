package it.neutro.assist.assessment;

import it.neutro.assist.shared.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final UserAssessmentRepository assessmentRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public AssessmentResponse saveOrUpdate(AssessmentRequest req) {
        Long userId = currentUserService.getCurrentUserId();
        int dailyCalorieTarget = CalorieCalculator.calculateDailyTarget(req);

        UserAssessment assessment = assessmentRepository.findByUserId(userId)
                .orElse(UserAssessment.builder().userId(userId).build());

        assessment.setFullName(req.fullName());
        assessment.setAge(req.age());
        assessment.setGender(req.gender());
        assessment.setHeightCm(req.heightCm());
        assessment.setWeightKg(req.weightKg());
        assessment.setActivityLevel(req.activityLevel());
        assessment.setGoal(req.goal());
        assessment.setDietaryPreferences(req.dietaryPreferences());
        assessment.setAllergies(req.allergies());
        assessment.setMedicalConditions(req.medicalConditions());
        assessment.setFoodInterests(req.foodInterests());
        assessment.setDailyCalorieTarget(dailyCalorieTarget);
        assessment.setCompleted(true);

        return AssessmentResponse.from(assessmentRepository.save(assessment));
    }

    public AssessmentResponse getMyAssessment() {
        Long userId = currentUserService.getCurrentUserId();
        return assessmentRepository.findByUserId(userId)
                .map(AssessmentResponse::from)
                .orElseThrow(() -> new IllegalStateException("Assessment not found"));
    }

    public AssessmentStatusResponse getStatus() {
        Long userId = currentUserService.getCurrentUserId();
        return assessmentRepository.findByUserId(userId)
                .map(a -> new AssessmentStatusResponse(a.isCompleted(), a.getDailyCalorieTarget()))
                .orElse(new AssessmentStatusResponse(false, 0));
    }
}
