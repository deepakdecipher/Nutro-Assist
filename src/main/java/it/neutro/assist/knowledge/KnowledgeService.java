package it.neutro.assist.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeService {

    private static final int CHUNK_WORD_SIZE    = 400;
    private static final int CHUNK_OVERLAP      = 50;
    private static final int MAX_CHUNKS_IN_PROMPT = 4;
    private static final int VALIDATION_SAMPLE  = 2_000;

    private final FileParserService fileParserService;
    private final KnowledgeSourceRepository sourceRepo;
    private final KnowledgeChunkRepository chunkRepo;
    private final RestClient groqClient;

    public KnowledgeService(FileParserService fileParserService,
                            KnowledgeSourceRepository sourceRepo,
                            KnowledgeChunkRepository chunkRepo,
                            @Value("${groq.api-key}") String apiKey) {
        this.fileParserService = fileParserService;
        this.sourceRepo        = sourceRepo;
        this.chunkRepo         = chunkRepo;
        this.groqClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    // ── Upload & process ──────────────────────────────────────────────────

    @Transactional
    public KnowledgeSourceResponse processUpload(MultipartFile file) throws IOException {
        String extension = fileParserService.getExtension(file);
        String rawText   = fileParserService.extractText(file);

        if (rawText.isBlank()) {
            throw new IllegalArgumentException("The file appears to be empty or could not be read.");
        }

        // Duplicate file name check
        String fileName = file.getOriginalFilename();
        if (fileName != null && sourceRepo.existsByFileName(fileName)) {
            throw new IllegalArgumentException(
                    "A file named '" + fileName + "' already exists in the knowledge base. " +
                    "Please delete the existing entry first, or rename your file before uploading.");
        }

        // AI content check — reject files not related to nutrition
        checkNutritionContent(rawText, fileName);

        List<String> chunks = splitIntoChunks(rawText, CHUNK_WORD_SIZE, CHUNK_OVERLAP);

        KnowledgeSource source = KnowledgeSource.builder()
                .fileName(file.getOriginalFilename())
                .fileType(extension)
                .totalChunks(chunks.size())
                .build();
        sourceRepo.save(source);

        List<KnowledgeChunk> chunkEntities = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            chunkEntities.add(KnowledgeChunk.builder()
                    .source(source)
                    .content(chunks.get(i))
                    .chunkIndex(i)
                    .build());
        }
        chunkRepo.saveAll(chunkEntities);

        log.info("Processed '{}' → {} chunks", file.getOriginalFilename(), chunks.size());
        return toResponse(source);
    }

    // ── List / Delete ─────────────────────────────────────────────────────

    public List<KnowledgeSourceResponse> listSources() {
        return sourceRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deleteSource(Long id) {
        sourceRepo.deleteById(id);
    }

    // ── RAG: build context for a user query ──────────────────────────────

    public String buildKnowledgeContext(String query) {
        List<String> keywords = extractKeywords(query);
        if (keywords.isEmpty()) return "";

        String t1 = keywords.get(0);
        String t2 = keywords.size() > 1 ? keywords.get(1) : t1;
        String t3 = keywords.size() > 2 ? keywords.get(2) : t2;

        List<KnowledgeChunk> relevant = chunkRepo.searchByKeywords(t1, t2, t3, MAX_CHUNKS_IN_PROMPT);
        if (relevant.isEmpty()) return "";

        return relevant.stream()
                .map(KnowledgeChunk::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    // ── Groq nutrition validation ─────────────────────────────────────────

    private void checkNutritionContent(String text, String fileName) {
        String sample = text.length() > VALIDATION_SAMPLE
                ? text.substring(0, VALIDATION_SAMPLE)
                : text;

        String prompt = """
                You are a content classifier for a nutrition and diet application.
                Analyze the document excerpt below and decide if it is related to:
                  - Food, nutrition, diet, meal plans, recipes, ingredients
                  - Calories, macros, vitamins, minerals, health and wellness
                  - Food databases, nutritional facts, diet programs

                Be generous — classify as NUTRITION if there is any significant food or health content.
                Only classify as IRRELEVANT if the content is clearly unrelated to food or health
                (e.g., purely financial data, software code, sports stats, geography, movies, etc.)

                Document excerpt:
                """ + sample + """

                Reply with ONLY one word: NUTRITION or IRRELEVANT
                """;

        try {
            var body = new GroqRequest("llama-3.3-70b-versatile",
                    List.of(new Msg("user", prompt)), 0.0);

            GroqResponse response = groqClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response != null && !response.choices().isEmpty()) {
                String verdict = response.choices().getFirst().message().content().trim().toUpperCase();
                log.info("Content validation for '{}': {}", fileName, verdict);
                if (verdict.contains("IRRELEVANT")) {
                    throw new IllegalArgumentException(
                            "This file does not appear to contain nutrition-related content. " +
                            "Please upload files about food, recipes, ingredients, diet plans, or nutritional data. " +
                            "If you believe this is an error, contact support.");
                }
            }
        } catch (IllegalArgumentException e) {
            throw e; // propagate content rejection to controller
        } catch (Exception e) {
            // Network error, rate limit, bad API key — fail open so uploads are never blocked by infra issues
            log.warn("Groq content validation unavailable for '{}', skipping check: {}", fileName, e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private List<String> splitIntoChunks(String text, int chunkSize, int overlap) {
        String[] words = text.trim().split("\\s+");
        List<String> chunks = new ArrayList<>();
        int step = chunkSize - overlap;
        for (int i = 0; i < words.length; i += step) {
            int end = Math.min(i + chunkSize, words.length);
            String chunk = String.join(" ", Arrays.copyOfRange(words, i, end)).trim();
            if (!chunk.isBlank()) chunks.add(chunk);
            if (end == words.length) break;
        }
        return chunks;
    }

    private List<String> extractKeywords(String query) {
        String[] stopWords = {"what","should","i","eat","for","the","a","an","is","are","how","can",
                "do","does","tell","me","my","about","please","give","best","good","help"};
        return Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(w -> w.length() > 3)
                .filter(w -> Arrays.stream(stopWords).noneMatch(sw -> sw.equals(w)))
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
    }

    private KnowledgeSourceResponse toResponse(KnowledgeSource s) {
        return new KnowledgeSourceResponse(s.getId(), s.getFileName(), s.getFileType(),
                s.getTotalChunks(), s.getUploadedAt());
    }

    // ── Groq request/response records ────────────────────────────────────

    record GroqRequest(String model, List<Msg> messages, double temperature) {}
    record Msg(String role, String content) {}
    record GroqResponse(List<Choice> choices) {}
    record Choice(MsgContent message) {}
    record MsgContent(String content) {}
}
