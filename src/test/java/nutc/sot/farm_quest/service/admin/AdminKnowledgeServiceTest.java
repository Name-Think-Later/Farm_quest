package nutc.sot.farm_quest.service.admin;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.admin.KnowledgeDocumentRequest;
import nutc.sot.farm_quest.dto.admin.ReindexKnowledgeRequest;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import nutc.sot.farm_quest.persistence.entity.LocationEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import nutc.sot.farm_quest.persistence.repository.KnowledgeDocumentRepository;
import nutc.sot.farm_quest.persistence.repository.LocationRepository;
import nutc.sot.farm_quest.persistence.repository.QuestRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminKnowledgeServiceTest {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository = mock(KnowledgeDocumentRepository.class);
    private final QuestRepository questRepository = mock(QuestRepository.class);
    private final LocationRepository locationRepository = mock(LocationRepository.class);
    private final GameRepository gameRepository = mock(GameRepository.class);
    private final AdminKnowledgeReindexService adminKnowledgeReindexService = mock(AdminKnowledgeReindexService.class);
    private final AuthProperties authProperties = authProperties();
    private final AdminKnowledgeService service = new AdminKnowledgeService(
            knowledgeDocumentRepository,
            questRepository,
            locationRepository,
            gameRepository,
            adminKnowledgeReindexService,
            authProperties
    );

    @Test
    void createKnowledgeDocumentStoresPendingDocumentAndQueuesReindex() {
        GameEntity game = game();
        QuestEntity quest = quest(game);
        when(gameRepository.findByCode("farm-quest")).thenReturn(Optional.of(game));
        when(questRepository.findById(quest.getId())).thenReturn(Optional.of(quest));
        when(knowledgeDocumentRepository.findByQuest_IdOrderByVersionDescUpdatedAtDesc(quest.getId())).thenReturn(List.of());
        when(knowledgeDocumentRepository.save(any(KnowledgeDocumentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createKnowledgeDocument(new KnowledgeDocumentRequest(
                "茶園知識",
                "這是內容",
                "admin://doc-1",
                quest.getId(),
                null,
                "LOW"
        ));

        assertThat(response.embeddingStatus()).isEqualTo("PENDING");
        assertThat(response.version()).isEqualTo(1);
        verify(knowledgeDocumentRepository).save(any(KnowledgeDocumentEntity.class));
        verify(adminKnowledgeReindexService).triggerReindexAsync(any());
    }

    @Test
    void reindexKnowledgeQueuesPendingDocumentsBeforeFailedOnes() {
        GameEntity game = game();
        KnowledgeDocumentEntity pendingDocument = document(game, quest(game), null);
        pendingDocument.setEmbeddingStatus("PENDING");
        KnowledgeDocumentEntity failedDocument = document(game, quest(game), null);
        failedDocument.setId(UUID.fromString("77777777-7777-7777-7777-777777777777"));
        failedDocument.setEmbeddingStatus("FAILED");
        when(gameRepository.findByCode("farm-quest")).thenReturn(Optional.of(game));
        when(knowledgeDocumentRepository.findByGame_IdAndEmbeddingStatusOrderByUpdatedAtDesc(game.getId(), "PENDING"))
                .thenReturn(List.of(pendingDocument));
        when(knowledgeDocumentRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.reindexKnowledge(new ReindexKnowledgeRequest(false));

        assertThat(response.accepted()).isTrue();
        assertThat(response.queuedDocumentCount()).isEqualTo(1);
        assertThat(response.status()).isEqualTo("REINDEX_QUEUED");
        assertThat(pendingDocument.getEmbeddingStatus()).isEqualTo("PENDING");
        verify(adminKnowledgeReindexService).triggerReindexAsync(List.of(pendingDocument.getId()));
    }

    @Test
    void reindexKnowledgeQueuesFailedDocumentsWhenNoPendingDocumentsExist() {
        GameEntity game = game();
        KnowledgeDocumentEntity failedDocument = document(game, quest(game), null);
        failedDocument.setEmbeddingStatus("FAILED");
        when(gameRepository.findByCode("farm-quest")).thenReturn(Optional.of(game));
        when(knowledgeDocumentRepository.findByGame_IdAndEmbeddingStatusOrderByUpdatedAtDesc(game.getId(), "PENDING"))
                .thenReturn(List.of());
        when(knowledgeDocumentRepository.findByGame_IdAndEmbeddingStatusOrderByUpdatedAtDesc(game.getId(), "FAILED"))
                .thenReturn(List.of(failedDocument));
        when(knowledgeDocumentRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.reindexKnowledge(new ReindexKnowledgeRequest(false));

        assertThat(response.queuedDocumentCount()).isEqualTo(1);
        assertThat(response.status()).isEqualTo("REINDEX_QUEUED");
        assertThat(failedDocument.getEmbeddingStatus()).isEqualTo("PENDING");
        verify(adminKnowledgeReindexService).triggerReindexAsync(List.of(failedDocument.getId()));
    }

    @Test
    void reindexKnowledgeReturnsZeroWhenNoPendingOrFailedDocumentsExist() {
        GameEntity game = game();
        when(gameRepository.findByCode("farm-quest")).thenReturn(Optional.of(game));
        when(knowledgeDocumentRepository.findByGame_IdAndEmbeddingStatusOrderByUpdatedAtDesc(game.getId(), "PENDING"))
                .thenReturn(List.of());
        when(knowledgeDocumentRepository.findByGame_IdAndEmbeddingStatusOrderByUpdatedAtDesc(game.getId(), "FAILED"))
                .thenReturn(List.of());

        var response = service.reindexKnowledge(new ReindexKnowledgeRequest(false));

        assertThat(response.accepted()).isTrue();
        assertThat(response.queuedDocumentCount()).isZero();
        assertThat(response.status()).isEqualTo("REINDEX_QUEUED");
    }

    @Test
    void reindexKnowledgeQueuesAllDocumentsWhenFullRebuildIsRequested() {
        GameEntity game = game();
        KnowledgeDocumentEntity document = document(game, quest(game), null);
        document.setEmbeddingStatus("INDEXED");
        when(gameRepository.findByCode("farm-quest")).thenReturn(Optional.of(game));
        when(knowledgeDocumentRepository.findByGame_IdOrderByUpdatedAtDesc(game.getId())).thenReturn(List.of(document));
        when(knowledgeDocumentRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.reindexKnowledge(new ReindexKnowledgeRequest(true));

        assertThat(response.status()).isEqualTo("REINDEX_QUEUED");
        assertThat(response.queuedDocumentCount()).isEqualTo(1);
        assertThat(document.getEmbeddingStatus()).isEqualTo("PENDING");
        verify(adminKnowledgeReindexService).triggerReindexAsync(List.of(document.getId()));
    }

    private AuthProperties authProperties() {
        AuthProperties properties = new AuthProperties();
        properties.setGameCode("farm-quest");
        return properties;
    }

    private GameEntity game() {
        GameEntity game = new GameEntity();
        game.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        game.setCode("farm-quest");
        return game;
    }

    private QuestEntity quest(GameEntity game) {
        QuestEntity quest = new QuestEntity();
        quest.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        quest.setGame(game);
        quest.setTitle("茶園任務");
        return quest;
    }

    private KnowledgeDocumentEntity document(GameEntity game, QuestEntity quest, LocationEntity location) {
        KnowledgeDocumentEntity document = new KnowledgeDocumentEntity();
        document.setId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        document.setGame(game);
        document.setQuest(quest);
        document.setLocation(location);
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
