package it.neutro.assist.ping;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@EnableScheduling
public class RenderKeepAliveScheduler {

    private static final String SWAGGER_JSON_URL = "https://nutro-assist.onrender.com";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Scheduled(fixedRate = 720000)
    public void keepNutroAssistAlive() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SWAGGER_JSON_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Render Keep-Alive Status Code: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Ping failed for nutro-assist: " + e.getMessage());
        }
    }
}
