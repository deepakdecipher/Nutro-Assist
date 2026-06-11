package it.neutro.assist.dietplan;

import it.neutro.assist.assessment.UserAssessment;
import it.neutro.assist.assessment.UserAssessmentRepository;
import it.neutro.assist.foodlog.FoodLog;
import it.neutro.assist.foodlog.FoodLogRepository;
import it.neutro.assist.shared.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DietPlanService {

    private final DietPlanRepository dietPlanRepository;
    private final DietPlanTemplateRepository templateRepository;
    private final TemplateDayRepository templateDayRepository;
    private final PlanDayRepository planDayRepository;
    private final FoodLogRepository foodLogRepository;
    private final UserAssessmentRepository assessmentRepository;
    private final AiPlanGeneratorService aiPlanGeneratorService;
    private final DietTemplateExcelParser excelParser;
    private final CurrentUserService currentUserService;

    // ── Active plan ────────────────────────────────────────────────────────────

    @Transactional
    public Optional<WeekViewResponse> getActivePlanWeekView() {
        Long userId = currentUserService.getCurrentUserId();
        Optional<DietPlan> planOpt = dietPlanRepository.findActivePlan(userId);

        if (planOpt.isEmpty()) return Optional.empty();

        DietPlan plan = planOpt.get();
        if (plan.isExpired()) {
            plan.setPlanStatus(PlanStatus.EXPIRED);
            dietPlanRepository.save(plan);
            return Optional.empty();
        }

        return Optional.of(buildWeekView(plan, userId));
    }

    public WeekViewResponse getWeekView(Long planId) {
        Long userId = currentUserService.getCurrentUserId();
        DietPlan plan = requireOwned(planId, userId);
        return buildWeekView(plan, userId);
    }

    public PlanDayResponse getDayView(Long planId, LocalDate date) {
        Long userId = currentUserService.getCurrentUserId();
        DietPlan plan = requireOwned(planId, userId);

        PlanDay day = planDayRepository.findByPlanIdAndPlanDate(plan.getId(), date)
                .orElseThrow(() -> new IllegalArgumentException("Day not found in plan"));

        List<FoodLog> logs = foodLogRepository.findByUserIdAndPlanDayId(userId, day.getId());
        Map<Long, FoodLog> logByMealId = logs.stream()
                .collect(Collectors.toMap(FoodLog::getPlanMealId, f -> f));

        return buildDayResponse(day, logByMealId);
    }

    // ── History ────────────────────────────────────────────────────────────────

    public List<PlanSummaryResponse> getHistory() {
        Long userId = currentUserService.getCurrentUserId();
        return dietPlanRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(p -> new PlanSummaryResponse(
                        p.getId(), p.getPlanType(), p.getPlanStatus(),
                        p.getDailyCalorieTarget(), p.getStartDate(), p.getEndDate(), p.getCreatedAt()))
                .toList();
    }

    // ── Generate AI plan ───────────────────────────────────────────────────────

    @Transactional
    public WeekViewResponse generateAiPlan() {
        Long userId = currentUserService.getCurrentUserId();
        ensureNoActivePlan(userId);

        UserAssessment assessment = assessmentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Complete your assessment before generating a plan"));

        log.info("Generating AI diet plan for user {}", userId);
        List<AiPlanGeneratorService.AiPlanDay> aiDays = aiPlanGeneratorService.generate30DayPlan(assessment);

        DietPlan plan = savePlan(userId, PlanType.AI_GENERATED, null,
                assessment.getDailyCalorieTarget(), 30, aiDays);

        return buildWeekView(plan, userId);
    }

    // ── Assign nutritionist plan ───────────────────────────────────────────────

    @Transactional
    public WeekViewResponse assignNutritionistPlan(Long templateId) {
        Long userId = currentUserService.getCurrentUserId();
        ensureNoActivePlan(userId);

        DietPlanTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        UserAssessment assessment = assessmentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Complete your assessment before purchasing a plan"));

        List<TemplateDay> templateDays = templateDayRepository.findByTemplateIdOrderByDayNumber(template.getId());
        List<AiPlanGeneratorService.AiPlanDay> aiDays = templateDays.stream().map(td -> {
            List<AiPlanGeneratorService.AiPlanMeal> meals = td.getMeals().stream().map(tm ->
                    new AiPlanGeneratorService.AiPlanMeal(
                            tm.getMealType(), tm.getMealName(), tm.getDescription(),
                            tm.getCalories(), tm.getProteinG(), tm.getCarbsG(), tm.getFatG())
            ).toList();
            return new AiPlanGeneratorService.AiPlanDay(td.getDayNumber(), meals);
        }).toList();

        DietPlan plan = savePlan(userId, PlanType.NUTRITIONIST, template.getId(),
                assessment.getDailyCalorieTarget(), template.getTotalDays(), aiDays);

        return buildWeekView(plan, userId);
    }

    // ── Admin: upload template ─────────────────────────────────────────────────

    @Transactional
    public DietPlanTemplate uploadTemplate(String name, String goal, MultipartFile file) {
        Long adminId = currentUserService.getCurrentUserId();
        it.neutro.assist.assessment.GoalType goalType =
                it.neutro.assist.assessment.GoalType.valueOf(goal.toUpperCase());

        List<DietTemplateExcelParser.ParsedRow> rows = excelParser.parse(file);
        if (rows.isEmpty()) throw new IllegalArgumentException("Excel file is empty or has no data rows");

        int totalDays = rows.stream().mapToInt(DietTemplateExcelParser.ParsedRow::day).max().orElse(0);

        DietPlanTemplate template = DietPlanTemplate.builder()
                .name(name)
                .goal(goalType)
                .totalDays(totalDays)
                .uploadedBy(adminId)
                .build();

        // Group rows by day
        Map<Integer, List<DietTemplateExcelParser.ParsedRow>> byDay = rows.stream()
                .collect(Collectors.groupingBy(DietTemplateExcelParser.ParsedRow::day));

        for (Map.Entry<Integer, List<DietTemplateExcelParser.ParsedRow>> entry : byDay.entrySet()) {
            TemplateDay day = TemplateDay.builder()
                    .template(template)
                    .dayNumber(entry.getKey())
                    .build();
            entry.getValue().forEach(r -> {
                TemplateMeal meal = TemplateMeal.builder()
                        .day(day)
                        .mealType(r.mealType())
                        .mealName(r.mealName())
                        .description(r.description())
                        .calories(r.calories())
                        .proteinG(r.proteinG())
                        .carbsG(r.carbsG())
                        .fatG(r.fatG())
                        .displayOrder(r.displayOrder())
                        .build();
                day.getMeals().add(meal);
            });
            template.getDays().add(day);
        }

        return templateRepository.save(template);
    }

    public List<TemplateSummaryResponse> listTemplates() {
        return templateRepository.findAll().stream()
                .map(t -> new TemplateSummaryResponse(t.getId(), t.getName(), t.getGoal(),
                        t.getTotalDays(), t.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) throw new IllegalArgumentException("Template not found");
        templateRepository.deleteById(id);
    }

    // ── Internal helpers ───────────────────────────────────────────────────────

    private DietPlan savePlan(Long userId, PlanType type, Long templateId,
                               int dailyCalorieTarget, int durationDays,
                               List<AiPlanGeneratorService.AiPlanDay> aiDays) {
        LocalDate start = LocalDate.now();
        LocalDate end   = start.plusDays(durationDays - 1);

        DietPlan plan = DietPlan.builder()
                .userId(userId)
                .planType(type)
                .planStatus(PlanStatus.ACTIVE)
                .dailyCalorieTarget(dailyCalorieTarget)
                .startDate(start)
                .endDate(end)
                .sourceTemplateId(templateId)
                .build();

        Map<Integer, AiPlanGeneratorService.AiPlanDay> dayMap = aiDays.stream()
                .collect(Collectors.toMap(AiPlanGeneratorService.AiPlanDay::dayNumber, d -> d));

        int templateSize = aiDays.size();
        for (int i = 0; i < durationDays; i++) {
            LocalDate date = start.plusDays(i);
            int dayNumber  = i + 1;
            int templateDay = ((i % templateSize) + 1);
            AiPlanGeneratorService.AiPlanDay aiDay = dayMap.get(templateDay);
            if (aiDay == null) continue;

            PlanDay planDay = PlanDay.builder()
                    .plan(plan)
                    .dayNumber(dayNumber)
                    .planDate(date)
                    .build();

            int order = 0;
            for (AiPlanGeneratorService.AiPlanMeal m : aiDay.meals()) {
                PlanMeal meal = PlanMeal.builder()
                        .planDay(planDay)
                        .mealType(m.mealType())
                        .mealName(m.mealName())
                        .description(m.description())
                        .calories(m.calories())
                        .proteinG(m.proteinG())
                        .carbsG(m.carbsG())
                        .fatG(m.fatG())
                        .displayOrder(order++)
                        .build();
                planDay.getMeals().add(meal);
            }
            plan.getDays().add(planDay);
        }

        return dietPlanRepository.save(plan);
    }

    private WeekViewResponse buildWeekView(DietPlan plan, Long userId) {
        LocalDate today  = LocalDate.now();
        LocalDate anchor = today.isBefore(plan.getStartDate()) ? plan.getStartDate() : today;
        LocalDate from   = anchor;
        LocalDate to     = anchor.plusDays(6);
        if (to.isAfter(plan.getEndDate())) to = plan.getEndDate();

        List<PlanDay> weekDays = planDayRepository.findWeek(plan.getId(), from, to);
        List<Long> dayIds = weekDays.stream().map(PlanDay::getId).toList();
        Set<Long> loggedMealIds = dayIds.isEmpty()
                ? Collections.emptySet()
                : foodLogRepository.findLoggedMealIds(userId, dayIds);

        Map<Long, List<FoodLog>> logsByDay = new HashMap<>();
        if (!dayIds.isEmpty()) {
            for (Long dayId : dayIds) {
                logsByDay.put(dayId, foodLogRepository.findByUserIdAndPlanDayId(userId, dayId));
            }
        }

        int todayConsumed = 0;
        List<PlanDayResponse> dayResponses = new ArrayList<>();
        for (PlanDay pd : weekDays) {
            List<FoodLog> dayLogs = logsByDay.getOrDefault(pd.getId(), List.of());
            Map<Long, FoodLog> logByMealId = dayLogs.stream()
                    .collect(Collectors.toMap(FoodLog::getPlanMealId, f -> f));

            PlanDayResponse dayResp = buildDayResponse(pd, logByMealId);
            dayResponses.add(dayResp);
            if (pd.getPlanDate().equals(today)) {
                todayConsumed = dayResp.totalConsumedCalories();
            }
        }

        return new WeekViewResponse(
                plan.getId(), plan.getPlanType(), plan.getPlanStatus(),
                plan.getDailyCalorieTarget(), plan.getStartDate(), plan.getEndDate(),
                todayConsumed, dayResponses);
    }

    private PlanDayResponse buildDayResponse(PlanDay pd, Map<Long, FoodLog> logByMealId) {
        List<PlanMeal> sortedMeals = pd.getMeals().stream()
                .sorted(Comparator.comparingInt(PlanMeal::getDisplayOrder))
                .toList();

        List<PlanMealResponse> mealResponses = sortedMeals.stream().map(m -> {
            FoodLog log = logByMealId.get(m.getId());
            boolean isLogged = log != null;
            return new PlanMealResponse(
                    m.getId(), m.getMealType(), m.getMealName(), m.getDescription(),
                    m.getCalories(), m.getProteinG(), m.getCarbsG(), m.getFatG(),
                    isLogged, isLogged ? log.getCaloriesConsumed() : 0
            );
        }).toList();

        int planned  = sortedMeals.stream().mapToInt(PlanMeal::getCalories).sum();
        int consumed = mealResponses.stream().mapToInt(PlanMealResponse::consumedCalories).sum();

        return new PlanDayResponse(
                pd.getId(), pd.getPlanDate(), pd.getDayNumber(),
                "Day " + pd.getDayNumber(),
                pd.getPlanDate().equals(LocalDate.now()),
                planned, consumed, mealResponses
        );
    }

    private void ensureNoActivePlan(Long userId) {
        dietPlanRepository.findActivePlan(userId).ifPresent(p -> {
            if (p.isExpired()) {
                p.setPlanStatus(PlanStatus.EXPIRED);
                dietPlanRepository.save(p);
            } else {
                throw new IllegalStateException("You already have an active diet plan. It expires on " + p.getEndDate());
            }
        });
    }

    private DietPlan requireOwned(Long planId, Long userId) {
        DietPlan plan = dietPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        if (!plan.getUserId().equals(userId)) throw new IllegalArgumentException("Plan not found");
        return plan;
    }
}
