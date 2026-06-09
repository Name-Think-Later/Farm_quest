package nutc.sot.farm_quest.service.quest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpringAiVectorStoreService implements VectorStoreService {

    private final VectorStore vectorStore;

    @Override
    public List<Document> search(String query, PromptPolicyService.PromptContext context) {
        try {
            return vectorStore.similaritySearch(buildSearchRequest(query, context));
        } catch (RuntimeException exception) {
            throw new QuestException(QuestErrorCode.RAG_RETRIEVAL_FAILED, HttpStatus.SERVICE_UNAVAILABLE, "RAG retrieval failed", exception);
        }
    }

    @Override
    public void indexKnowledgeDocuments(List<KnowledgeDocumentEntity> knowledgeDocuments) {
        if (knowledgeDocuments.isEmpty()) {
            return;
        }
        List<Document> documents = knowledgeDocuments.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());
        log.info("Indexing {} knowledge documents into vector store: {}", documents.size(), documents.stream()
                .map(Document::getId)
                .toList());
        for (Document document : documents) {
            log.info("Prepared knowledge document id={}, textLength={}, metadataKeys={}",
                    document.getId(),
                    document.getText() == null ? 0 : document.getText().length(),
                    document.getMetadata().keySet());
        }
        try {
            vectorStore.add(documents);
        } catch (RuntimeException exception) {
            log.error("Knowledge indexing failed while writing documents {} to vector store", documents.stream()
                    .map(Document::getId)
                    .toList(), exception);
            throw new QuestException(QuestErrorCode.RAG_RETRIEVAL_FAILED, HttpStatus.SERVICE_UNAVAILABLE, "Knowledge indexing failed", exception);
        }
    }

    SearchRequest buildSearchRequest(String query, PromptPolicyService.PromptContext context) {
        return SearchRequest.builder()
                .query(query)
                .topK(4)
                .filterExpression("gameId == '" + asFilterValue(context.quest().getGame().getId()) + "' && questId == '" + asFilterValue(context.quest().getId()) + "'")
                .build();
    }

    private Document toDocument(KnowledgeDocumentEntity knowledgeDocument) {
        Map<String, Object> metadata = Map.of(
                "gameId", knowledgeDocument.getGame().getId().toString(),
                "documentId", knowledgeDocument.getId().toString(),
                "questId", knowledgeDocument.getQuest() == null ? "" : knowledgeDocument.getQuest().getId().toString(),
                "locationId", knowledgeDocument.getLocation() == null ? "" : knowledgeDocument.getLocation().getId().toString(),
                "title", knowledgeDocument.getTitle(),
                "source", knowledgeDocument.getSource(),
                "spoilerLevel", knowledgeDocument.getSpoilerLevel(),
                "version", knowledgeDocument.getVersion().toString()
        );
        return new Document(documentId(knowledgeDocument), knowledgeText(knowledgeDocument), metadata);
    }

    private String knowledgeText(KnowledgeDocumentEntity knowledgeDocument) {
        String content = knowledgeDocument.getContent();
        if (content == null || content.isBlank()) {
            return knowledgeDocument.getTitle();
        }
        return knowledgeDocument.getTitle() + "\n\n" + content.trim();
    }

    private String documentId(KnowledgeDocumentEntity knowledgeDocument) {
        return knowledgeDocument.getId().toString();
    }

    private String asFilterValue(UUID value) {
        return value.toString();
    }
}
