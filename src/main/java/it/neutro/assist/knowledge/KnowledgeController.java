package it.neutro.assist.knowledge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/knowledge")
@PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(knowledgeService.processUpload(file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            log.error("File parsing failed for '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to read the file: " + e.getMessage() +
                          ". Please ensure the file is not corrupted and try again.");
        }
    }

    @GetMapping("/sources")
    public ResponseEntity<List<KnowledgeSourceResponse>> list() {
        return ResponseEntity.ok(knowledgeService.listSources());
    }

    @DeleteMapping("/sources/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        knowledgeService.deleteSource(id);
        return ResponseEntity.noContent().build();
    }
}
