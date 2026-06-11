package it.neutro.assist.knowledge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    @Query(value = """
            SELECT kc.* FROM knowledge_chunks kc
            WHERE kc.content ILIKE '%' || :term1 || '%'
               OR kc.content ILIKE '%' || :term2 || '%'
               OR kc.content ILIKE '%' || :term3 || '%'
            ORDER BY (
                (CASE WHEN kc.content ILIKE '%' || :term1 || '%' THEN 1 ELSE 0 END) +
                (CASE WHEN kc.content ILIKE '%' || :term2 || '%' THEN 1 ELSE 0 END) +
                (CASE WHEN kc.content ILIKE '%' || :term3 || '%' THEN 1 ELSE 0 END)
            ) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeChunk> searchByKeywords(@Param("term1") String term1,
                                          @Param("term2") String term2,
                                          @Param("term3") String term3,
                                          @Param("limit") int limit);
}
