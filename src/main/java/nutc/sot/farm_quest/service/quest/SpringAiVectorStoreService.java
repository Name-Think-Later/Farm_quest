package nutc.sot.farm_quest.service.quest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nutc.sot.farm_quest.config.QdrantProperties;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpringAiVectorStoreService implements VectorStoreService {

    private static final String QDRANT_CONTENT_FIELD_NAME = "doc_content";

    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;
    private final QdrantProperties qdrantProperties;
    @Qualifier("qdrantRestClient")
    private final RestClient qdrantRestClient;

    @Override
    public List<Document> search(String query, PromptPolicyService.PromptContext context) {
        try {
            return vectorStore.similaritySearch(buildSearchRequest(query, context));
        } catch (RuntimeException exception) {
            throw new QuestException(QuestErrorCode.RAG_RETRIEVAL_FAILED, HttpStatus.SERVICE_UNAVAILABLE, "RAG retrieval failed", exception);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (documents.isEmpty()) {
            return;
        }
        log.info("Indexing {} chunks into vector store: {}", documents.size(), documents.stream()
                .map(Document::getId)
                .toList());
        for (Document document : documents) {
            log.info("Prepared knowledge chunk id={}, textLength={}, metadataKeys={}",
                    document.getId(),
                    document.getText() == null ? 0 : document.getText().length(),
                    document.getMetadata().keySet());
        }
        try {
            if (qdrantProperties.enabled()) {
                upsertDocuments(documents);
                return;
            }
            vectorStore.add(documents);
        } catch (QuestException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("Knowledge indexing failed while writing chunks {} to vector store", documents.stream()
                    .map(Document::getId)
                    .toList(), exception);
            throw new QuestException(QuestErrorCode.RAG_RETRIEVAL_FAILED, HttpStatus.SERVICE_UNAVAILABLE, "Knowledge indexing failed", exception);
        }
    }

    private void upsertDocuments(List<Document> documents) {
        EmbeddingModel embeddingModel = embeddingService.model();
        List<Map<String, Object>> points = documents.stream()
                .map(document -> toPoint(document, embeddingModel))
                .toList();

        try {
            qdrantRestClient.put()
                    .uri("/collections/{collectionName}/points?wait=true", qdrantProperties.collectionName())
                    .headers(this::applyApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("points", points))
                    .retrieve()
                    .toBodilessEntity();

        } catch (RuntimeException exception) {
            throw new QuestException(QuestErrorCode.RAG_RETRIEVAL_FAILED, HttpStatus.SERVICE_UNAVAILABLE, "Knowledge indexing failed", exception);
        }
    }

    private Map<String, Object> toPoint(Document document, EmbeddingModel embeddingModel) {
        float[] embedding = embeddingModel.embed(document);
        validateEmbedding(document, embedding);
        log.info("Embedding generated for knowledge chunk id={}, vectorLength={}", document.getId(), embedding.length);

        Map<String, Object> payload = new HashMap<>(document.getMetadata());
        payload.put(QDRANT_CONTENT_FIELD_NAME, document.getText() == null ? "" : document.getText());

        return Map.of(
                "id", document.getId(),
                "vector", asFloatList(embedding),
                "payload", payload
        );
    }

    private void validateEmbedding(Document document, float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            throw new QuestException(
                    QuestErrorCode.RAG_RETRIEVAL_FAILED,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Embedding model returned empty vector for knowledge chunk " + document.getId());
        }
        if (qdrantProperties.vectorSize() > 0 && embedding.length != qdrantProperties.vectorSize()) {
            throw new QuestException(
                    QuestErrorCode.RAG_RETRIEVAL_FAILED,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Embedding vector dimension mismatch for knowledge chunk " + document.getId()
                            + ": expected " + qdrantProperties.vectorSize() + " but got " + embedding.length);
        }
    }

    private List<Float> asFloatList(float[] embedding) {
        Float[] boxed = new Float[embedding.length];
        for (int i = 0; i < embedding.length; i++) {
            boxed[i] = embedding[i];
        }
        return List.of(boxed);
    }

    private void applyApiKey(HttpHeaders headers) {
        if (StringUtils.hasText(qdrantProperties.apiKey())) {
            headers.set("api-key", qdrantProperties.apiKey());
        }
    }

    SearchRequest buildSearchRequest(String query, PromptPolicyService.PromptContext context) {
        return SearchRequest.builder()
                .query(query)
                .topK(4)
                .filterExpression("gameId == '" + asFilterValue(context.quest().getGame().getId()) + "' && questId == '" + asFilterValue(context.quest().getId()) + "'")
                .build();
    }

    private String asFilterValue(UUID value) {
        return value.toString();
    }
}
