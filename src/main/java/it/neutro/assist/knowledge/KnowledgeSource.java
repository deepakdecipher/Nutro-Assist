package it.neutro.assist.knowledge;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "knowledge_sources")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnowledgeSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 50)
    private String fileType;

    @Column(nullable = false)
    private int totalChunks;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<KnowledgeChunk> chunks = new ArrayList<>();

    @PrePersist
    void prePersist() {
        uploadedAt = LocalDateTime.now();
    }
}
