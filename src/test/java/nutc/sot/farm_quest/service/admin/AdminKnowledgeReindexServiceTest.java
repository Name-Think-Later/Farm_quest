package nutc.sot.farm_quest.service.admin;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.repository.KnowledgeDocumentRepository;
import nutc.sot.farm_quest.service.quest.KnowledgeIndexingService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminKnowledgeReindexServiceTest {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository = mock(KnowledgeDocumentRepository.class);
    private final KnowledgeIndexingService knowledgeIndexingService = mock(KnowledgeIndexingService.class);
    private final AdminKnowledgeReindexService service = new AdminKnowledgeReindexService(knowledgeDocumentRepository, knowledgeIndexingService);

    @Test
    void triggerReindexAsyncMarksDocumentsIndexedWhenVectorStoreSucceeds() {
        KnowledgeDocumentEntity document = document();
        when(knowledgeDocumentRepository.findAllById(List.of(document.getId()))).thenReturn(List.of(document));
        when(knowledgeDocumentRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(knowledgeIndexingService).indexKnowledgeDocuments(any());

        service.triggerReindexAsync(List.of(document.getId()));

        assertThat(document.getEmbeddingStatus()).isEqualTo("INDEXED");
        assertThat(document.getIndexedAt()).isNotNull();
        verify(knowledgeIndexingService).indexKnowledgeDocuments(any());
    }

    @Test
    void triggerReindexAsyncMarksDocumentsFailedWhenVectorStoreThrows() {
        KnowledgeDocumentEntity document = document();
        when(knowledgeDocumentRepository.findAllById(List.of(document.getId()))).thenReturn(List.of(document));
        when(knowledgeDocumentRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("qdrant down")).when(knowledgeIndexingService).indexKnowledgeDocuments(any());

        service.triggerReindexAsync(List.of(document.getId()));

        assertThat(document.getEmbeddingStatus()).isEqualTo("FAILED");
    }

    private KnowledgeDocumentEntity document() {
        GameEntity game = new GameEntity();
        game.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        QuestEntity quest = new QuestEntity();
        quest.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        quest.setGame(game);

        KnowledgeDocumentEntity document = new KnowledgeDocumentEntity();
        document.setId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        document.setGame(game);
        document.setQuest(quest);
        document.setTitle("茶園知識");
        document.setContent("內容");
        document.setSource("admin://doc-1");
        document.setSpoilerLevel("LOW");
        document.setVersion(1);
        document.setEmbeddingStatus("PENDING");
        document.setUpdatedAt(OffsetDateTime.parse("2026-06-09T10:00:00+08:00"));
        return document;
    }
}
