package it.neutro.assist.knowledge;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "knowledge_chunks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private KnowledgeSource source;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int chunkIndex;
}
