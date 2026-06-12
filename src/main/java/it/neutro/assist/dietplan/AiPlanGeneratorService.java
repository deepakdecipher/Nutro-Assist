package it.neutro.assist.dietplan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.neutro.assist.assessment.UserAssessment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiPlanGeneratorService {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiPlanGeneratorService(@Value("${groq.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(GROQ_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public List<AiPlanDay> generate30DayPlan(UserAssessment assessment) {
        String prompt = buildPrompt(assessment);
        String rawJson = callGroq(prompt);
        return parsePlan(rawJson);
    }

    private String buildPrompt(UserAssessment a) {
        return """
                You are a professional nutritionist. Generate a 7-day personalized Indian diet plan.
                The system will automatically repeat these 7 days to cover 30 days, so focus on variety within the 7 days.

                User profile:
                - Name: %s
                - Age: %d, Gender: %s
                - Height: %.1f cm, Weight: %.1f kg
                - Activity level: %s
                - Goal: %s
                - Daily calorie target: %d kcal
                - Dietary preferences: %s
                - Allergies: %s
                - Medical conditions: %s
                - Food interests: %s

                CRITICAL RULES:
                - Return ONLY a raw JSON object. No markdown, no code blocks, no backticks, no explanation.
                - mealType values must be EXACTLY one of: BREAKFAST, MORNING_SNACK, LUNCH, EVENING_SNACK, DINNER
                - Each day must have all 5 meal types in that order
                - Total daily calories should be close to %d kcal
                - Generate exactly 7 days (dayNumber 1 through 7)

                Required JSON structure:
                {"days":[{"dayNumber":1,"meals":[{"mealType":"BREAKFAST","mealName":"...","description":"...","calories":350,"proteinG":12.0,"carbsG":45.0,"fatG":8.0}]}]}
                """.formatted(
                a.getFullName(), a.getAge(), a.getGender(),
                a.getHeightCm(), a.getWeightKg(),
                a.getActivityLevel(), a.getGoal(),
                a.getDailyCalorieTarget(),
                nvl(a.getDietaryPreferences()),
                nvl(a.getAllergies()),
                nvl(a.getMedicalConditions()),
                nvl(a.getFoodInterests()),
                a.getDailyCalorieTarget()
        );
    }

    private String callGroq(String userPrompt) {
        var body = new GroqRequest(MODEL,
                List.of(new Msg("user", userPrompt)), 0.3,
                Map.of("type", "json_object"));
        GroqResponse response = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(GroqResponse.class);
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new RuntimeException("Groq returned empty response when generating diet plan");
        }
        return response.choices().getFirst().message().content();
    }

    private List<AiPlanDay> parsePlan(String rawJson) {
        try {
            String json = extractJson(rawJson);
            JsonNode root = objectMapper.readTree(json);
            JsonNode daysNode = root.get("days");
            if (daysNode == null || !daysNode.isArray() || daysNode.isEmpty()) {
                log.error("Invalid plan structure. Raw response: {}", rawJson);
                throw new RuntimeException("AI returned an invalid plan structure");
            }
            List<AiPlanDay> days = new ArrayList<>();
            for (JsonNode dayNode : daysNode) {
                if (dayNode == null || !dayNode.isObject()) continue;
                JsonNode dayNumNode = dayNode.get("dayNumber");
                if (dayNumNode == null) continue;
                int dayNumber = dayNumNode.asInt();

                JsonNode mealsNode = dayNode.get("meals");
                if (mealsNode == null || !mealsNode.isArray()) continue;

                List<AiPlanMeal> meals = new ArrayList<>();
                for (JsonNode mealNode : mealsNode) {
                    if (mealNode == null) continue;
                    JsonNode typeNode = mealNode.get("mealType");
                    if (typeNode == null) continue;
                    MealType mealType;
                    try {
                        mealType = MealType.valueOf(typeNode.asText().toUpperCase().replace(" ", "_"));
                    } catch (IllegalArgumentException ex) {
                        log.warn("Skipping unknown mealType '{}' on day {}", typeNode.asText(), dayNumber);
                        continue;
                    }
                    meals.add(new AiPlanMeal(
                            mealType,
                            textOf(mealNode, "mealName", "Meal"),
                            textOf(mealNode, "description", ""),
                            intOf(mealNode, "calories", 300),
                            doubleOf(mealNode, "proteinG", 10.0),
                            doubleOf(mealNode, "carbsG", 40.0),
                            doubleOf(mealNode, "fatG", 8.0)
                    ));
                }
                if (!meals.isEmpty()) days.add(new AiPlanDay(dayNumber, meals));
            }
            if (days.isEmpty()) {
                log.error("Parser produced 0 days. Raw response: {}", rawJson);
                throw new RuntimeException("AI plan contained no valid days");
            }
            return days;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Groq plan JSON. Raw response: {}", rawJson, e);
            throw new RuntimeException("Failed to parse AI-generated diet plan. Please try again.");
        }
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start == -1 || end == -1 || end < start) {
            log.error("No JSON object found in Groq response: {}", raw);
            throw new RuntimeException("No JSON found in AI response");
        }
        return raw.substring(start, end + 1);
    }

    private static String textOf(JsonNode n, String key, String def) {
        JsonNode v = n.get(key);
        return (v != null && !v.isNull()) ? v.asText(def) : def;
    }

    private static int intOf(JsonNode n, String key, int def) {
        JsonNode v = n.get(key);
        return (v != null && !v.isNull()) ? v.asInt(def) : def;
    }

    private static double doubleOf(JsonNode n, String key, double def) {
        JsonNode v = n.get(key);
        return (v != null && !v.isNull()) ? v.asDouble(def) : def;
    }

    private static String nvl(String s) {
        return s == null ? "none" : s;
    }

    record AiPlanDay(int dayNumber, List<AiPlanMeal> meals) {}
    record AiPlanMeal(MealType mealType, String mealName, String description,
                      int calories, double proteinG, double carbsG, double fatG) {}

    record GroqRequest(String model, List<Msg> messages, double temperature,
                       @JsonProperty("response_format") Map<String, String> responseFormat) {}
    record Msg(String role, String content) {}
    record GroqResponse(List<Choice> choices) {}
    record Choice(MsgContent message) {}
    record MsgContent(String content) {}
}
