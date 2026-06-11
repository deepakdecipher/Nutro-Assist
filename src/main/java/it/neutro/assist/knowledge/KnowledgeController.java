package it.neutro.assist.knowledge;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/knowledge")
@PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/upload")
    public ResponseEntity<KnowledgeSourceResponse> upload(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(knowledgeService.processUpload(file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
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
