package it.neutro.assist.knowledge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private static final int CHUNK_WORD_SIZE = 400;
    private static final int CHUNK_OVERLAP   = 50;
    private static final int MAX_CHUNKS_IN_PROMPT = 4;

    private final FileParserService fileParserService;
    private final KnowledgeSourceRepository sourceRepo;
    private final KnowledgeChunkRepository chunkRepo;

    // ── Upload & process ──────────────────────────────────────────────────

    @Transactional
    public KnowledgeSourceResponse processUpload(MultipartFile file) throws IOException {
        String extension = fileParserService.getExtension(file);
        String rawText   = fileParserService.extractText(file);
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

        log.info("Processed file '{}' → {} chunks", file.getOriginalFilename(), chunks.size());
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

    // ── RAG: find relevant chunks for a user query ────────────────────────

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
        // Remove common stop words, keep meaningful nutrition-related terms
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
}
