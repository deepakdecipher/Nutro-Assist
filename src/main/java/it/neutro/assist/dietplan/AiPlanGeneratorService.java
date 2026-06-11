package it.neutro.assist.dietplan;

import com.fasterxml.jackson.core.JsonProcessingException;
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
                You are a professional nutritionist. Generate a 30-day personalized diet plan as strict JSON.

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

                Return ONLY valid JSON with this exact structure (no markdown, no explanations):
                {
                  "days": [
                    {
                      "dayNumber": 1,
                      "meals": [
                        {
                          "mealType": "BREAKFAST",
                          "mealName": "...",
                          "description": "...",
                          "calories": 350,
                          "proteinG": 12.0,
                          "carbsG": 45.0,
                          "fatG": 8.0
                        }
                      ]
                    }
                  ]
                }

                Meal types must be exactly: BREAKFAST, MORNING_SNACK, LUNCH, EVENING_SNACK, DINNER.
                Each day must have all 5 meal types. Total daily calories must be close to %d kcal.
                Generate all 30 days with varied meals.
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
                List.of(new Msg("user", userPrompt)), 0.7);
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
            if (daysNode == null || !daysNode.isArray()) {
                throw new RuntimeException("Invalid plan JSON: missing 'days' array");
            }
            List<AiPlanDay> days = new ArrayList<>();
            for (JsonNode dayNode : daysNode) {
                int dayNumber = dayNode.get("dayNumber").asInt();
                List<AiPlanMeal> meals = new ArrayList<>();
                for (JsonNode mealNode : dayNode.get("meals")) {
                    meals.add(new AiPlanMeal(
                            MealType.valueOf(mealNode.get("mealType").asText()),
                            mealNode.get("mealName").asText(),
                            mealNode.has("description") ? mealNode.get("description").asText() : "",
                            mealNode.get("calories").asInt(),
                            mealNode.get("proteinG").asDouble(),
                            mealNode.get("carbsG").asDouble(),
                            mealNode.get("fatG").asDouble()
                    ));
                }
                days.add(new AiPlanDay(dayNumber, meals));
            }
            return days;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Groq plan JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to parse AI-generated diet plan. Please try again.");
        }
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start == -1 || end == -1) throw new RuntimeException("No JSON found in Groq response");
        return raw.substring(start, end + 1);
    }

    private static String nvl(String s) {
        return s == null ? "none" : s;
    }

    record AiPlanDay(int dayNumber, List<AiPlanMeal> meals) {}
    record AiPlanMeal(MealType mealType, String mealName, String description,
                      int calories, double proteinG, double carbsG, double fatG) {}

    record GroqRequest(String model, List<Msg> messages, double temperature) {}
    record Msg(String role, String content) {}
    record GroqResponse(List<Choice> choices) {}
    record Choice(MsgContent message) {}
    record MsgContent(String content) {}
}
