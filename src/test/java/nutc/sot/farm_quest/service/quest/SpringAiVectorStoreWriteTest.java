package nutc.sot.farm_quest.service.quest;

import java.util.List;
import java.util.Map;
import nutc.sot.farm_quest.config.QdrantProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringAiVectorStoreWriteTest {

    private final VectorStore vectorStore = mock(VectorStore.class);
    private final EmbeddingService embeddingService = mock(EmbeddingService.class);
    private final EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
    private final RestClient qdrantRestClient = mock(RestClient.class);
    private final QdrantProperties qdrantProperties = new QdrantProperties(false, "http://localhost:6333", "", "knowledge-documents", 1024, "COSINE");
    private final SpringAiVectorStoreService service = new SpringAiVectorStoreService(vectorStore, embeddingService, qdrantProperties, qdrantRestClient);

    @Test
    void addDocumentsWritesChunksToVectorStore() {
        List<Document> documents = List.of(new Document("茶園內容", Map.of("documentId", "doc-1")));

        service.addDocuments(documents);

        verify(vectorStore).add(documents);
    }

    @Test
    void addDocumentsMapsVectorStoreErrorsToQuestException() {
        List<Document> documents = List.of(new Document("茶園內容", Map.of("documentId", "doc-1")));
        doThrow(new RuntimeException("boom")).when(vectorStore).add(any());

        assertThatThrownBy(() -> service.addDocuments(documents))
                .hasMessage("Knowledge indexing failed");
    }

    @Test
    void addDocumentsFailsFastWhenEmbeddingVectorIsEmptyForQdrantRestPath() {
        VectorStore nativeVectorStore = mock(VectorStore.class);
        RestClient nativeQdrantRestClient = mock(RestClient.class);
        when(embeddingService.model()).thenReturn(embeddingModel);
        when(embeddingModel.embed(any(Document.class))).thenReturn(new float[0]);
        SpringAiVectorStoreService nativeService = new SpringAiVectorStoreService(
                nativeVectorStore,
                embeddingService,
                new QdrantProperties(true, "http://localhost:6333", "", "knowledge-documents", 1024, "COSINE"),
                nativeQdrantRestClient
        );

        assertThatThrownBy(() -> nativeService.addDocuments(List.of(new Document("茶園內容", Map.of("documentId", "doc-1")))))
                .hasMessageContaining("Embedding model returned empty vector");
    }
}
