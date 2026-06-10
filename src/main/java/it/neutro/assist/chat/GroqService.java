package it.neutro.assist.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.MediaType;

import java.util.List;

@Slf4j
@Service
public class GroqService {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final String SYSTEM_PROMPT =
            "You are Nutro, a friendly AI nutrition coach. Help users with diet, nutrition, meal plans, "
            + "and health goals. Keep answers concise, warm, and practical. If asked something unrelated "
            + "to health or nutrition, gently steer back to those topics.";

    private final RestClient restClient;

    public GroqService(@Value("${groq.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(GROQ_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public String chat(String userMessage) {
        try {
            var body = new GroqRequest(
                    MODEL,
                    List.of(new Msg("system", SYSTEM_PROMPT), new Msg("user", userMessage)),
                    0.7
            );

            GroqResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                log.warn("Groq returned empty response for message: {}", userMessage);
                return "I'm having trouble respoasdnding right now. Please try again.";
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
