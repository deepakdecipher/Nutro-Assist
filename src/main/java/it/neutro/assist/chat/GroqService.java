package it.neutro.assist.chat;

import it.neutro.assist.knowledge.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Service
public class GroqService {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL    = "llama-3.3-70b-versatile";

    private static final String BASE_SYSTEM_PROMPT = """
            You are Nutro, an expert AI nutrition and diet coach.
            Your ONLY topics are: nutrition, diet, food, meal plans, calories, macros, \
            vitamins, minerals, weight management, healthy eating habits, hydration, \
            supplements, and related health topics.
            If asked about ANYTHING else (movies, sports, politics, coding, etc.), \
            respond ONLY with: "I can only help with nutrition and diet-related topics. \
            What would you like to know about your diet or health goals?"
            Be concise, warm, and science-based. Use bullet points for clarity when helpful.
            """;

    private final RestClient restClient;
    private final KnowledgeService knowledgeService;

    public GroqService(@Value("${groq.api-key}") String apiKey, KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
        this.restClient = RestClient.builder()
                .baseUrl(GROQ_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public String chat(String userMessage) {
        try {
            String knowledgeContext = knowledgeService.buildKnowledgeContext(userMessage);
            String systemPrompt = knowledgeContext.isBlank()
                    ? BASE_SYSTEM_PROMPT
                    : BASE_SYSTEM_PROMPT + "\n\nUse the following knowledge from our nutrition resources " +
                      "to answer accurately. Prefer this over general knowledge:\n\n" + knowledgeContext;

            var body = new GroqRequest(
                    MODEL,
                    List.of(new Msg("system", systemPrompt), new Msg("user", userMessage)),
                    0.7
            );

            GroqResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                log.warn("Groq returned empty response for message: {}", userMessage);
                return "I'm having trouble responding right now. Please try again.";
            }
            return response.choices().getFirst().message().content();

        } catch (RestClientException e) {
            log.error("Groq API call failed: {}", e.getMessage(), e);
            return "I'm having trouble responding right now. Please try again.";
        }
    }

    record GroqRequest(String model, List<Msg> messages, double temperature) {}
    record Msg(String role, String content) {}
    record GroqResponse(List<Choice> choices) {}
    record Choice(MsgContent message) {}
    record MsgContent(String content) {}
}
