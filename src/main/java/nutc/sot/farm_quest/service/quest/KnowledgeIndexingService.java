package nutc.sot.farm_quest.service.quest;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeIndexingService {

    private final KnowledgeChunkingService knowledgeChunkingService;
    private final VectorStoreService vectorStoreService;

    public void indexKnowledgeDocuments(List<KnowledgeDocumentEntity> knowledgeDocuments) {
        if (knowledgeDocuments.isEmpty()) {
            return;
        }

        List<Document> chunkedDocuments = knowledgeDocuments.stream()
                .flatMap(document -> knowledgeChunkingService.chunk(document).stream())
                .toList();

        log.info("Indexing {} knowledge documents as {} chunks", knowledgeDocuments.size(), chunkedDocuments.size());
        vectorStoreService.addDocuments(chunkedDocuments);
    }
}
