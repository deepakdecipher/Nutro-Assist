package it.neutro.assist.knowledge;

import java.time.LocalDateTime;

public record KnowledgeSourceResponse(Long id, String fileName, String fileType,
                                       int totalChunks, LocalDateTime uploadedAt) {}
