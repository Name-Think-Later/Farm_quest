package nutc.sot.farm_quest.service.admin;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import nutc.sot.farm_quest.persistence.repository.KnowledgeDocumentRepository;
import nutc.sot.farm_quest.service.quest.VectorStoreService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminKnowledgeReindexService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final VectorStoreService vectorStoreService;

    @Async
    @Transactional
    public void triggerReindexAsync(List<UUID> documentIds) {
        List<KnowledgeDocumentEntity> documents = knowledgeDocumentRepository.findAllById(documentIds).stream()
                .filter(document -> "PENDING".equals(document.getEmbeddingStatus()))
                .toList();
        if (documents.isEmpty()) {
            return;
        }

        try {
            vectorStoreService.indexKnowledgeDocuments(documents);
            OffsetDateTime indexedAt = OffsetDateTime.now();
            for (KnowledgeDocumentEntity document : documents) {
                document.setEmbeddingStatus("INDEXED");
                document.setIndexedAt(indexedAt);
                document.setUpdatedAt(indexedAt);
            }
            knowledgeDocumentRepository.saveAll(documents);
        } catch (RuntimeException exception) {
            log.warn("Knowledge reindex failed for documents {}", documentIds, exception);
            OffsetDateTime failedAt = OffsetDateTime.now();
            for (KnowledgeDocumentEntity document : documents) {
                document.setEmbeddingStatus("FAILED");
                document.setUpdatedAt(failedAt);
            }
            knowledgeDocumentRepository.saveAll(documents);
        }
    }
}
