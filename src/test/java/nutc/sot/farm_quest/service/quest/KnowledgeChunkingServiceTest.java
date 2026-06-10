package nutc.sot.farm_quest.service.quest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import nutc.sot.farm_quest.persistence.entity.LocationEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeChunkingServiceTest {

    private final KnowledgeChunkingService service = new KnowledgeChunkingService();

    @Test
    void chunkCreatesSingleChunkForShortDocument() {
        KnowledgeDocumentEntity document = knowledgeDocument("茶園知識", "簡短內容");

        List<Document> chunks = service.chunk(document);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst().getId()).isEqualTo(expectedChunkId(document.getId(), 0));
        assertThatCodeProducesUuid(chunks.getFirst().getId());
        assertThat(chunks.getFirst().getMetadata()).containsEntry("documentId", document.getId().toString());
        assertThat(chunks.getFirst().getMetadata()).containsEntry("chunkIndex", 0);
        assertThat(chunks.getFirst().getMetadata()).containsEntry("questId", document.getQuest().getId().toString());
        assertThat(chunks.getFirst().getMetadata()).containsEntry("gameId", document.getGame().getId().toString());
        assertThat(chunks.getFirst().getMetadata()).containsEntry("version", "1");
    }

    @Test
    void chunkSplitsLongDocumentIntoMultipleChunks() {
        KnowledgeDocumentEntity document = knowledgeDocument("茶園知識", "內容".repeat(2000));

        List<Document> chunks = service.chunk(document);

        assertThat(chunks.size()).isGreaterThan(1);
        assertThat(chunks.getFirst().getMetadata()).containsEntry("documentId", document.getId().toString());
        assertThat(chunks.get(1).getId()).isEqualTo(expectedChunkId(document.getId(), 1));
        assertThatCodeProducesUuid(chunks.get(1).getId());
        assertThat(chunks.get(1).getMetadata()).containsEntry("chunkIndex", 1);
    }

    private void assertThatCodeProducesUuid(String chunkId) {
        assertThat(UUID.fromString(chunkId)).isNotNull();
    }

    private String expectedChunkId(UUID documentId, int chunkIndex) {
        return UUID.nameUUIDFromBytes((documentId + ":" + chunkIndex).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private KnowledgeDocumentEntity knowledgeDocument(String title, String content) {
        GameEntity game = new GameEntity();
        game.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        QuestEntity quest = new QuestEntity();
        quest.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        quest.setGame(game);

        LocationEntity location = new LocationEntity();
        location.setId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        location.setQuest(quest);

        KnowledgeDocumentEntity document = new KnowledgeDocumentEntity();
        document.setId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        document.setGame(game);
        document.setQuest(quest);
        document.setLocation(location);
        document.setTitle(title);
        document.setContent(content);
        document.setSource("admin://doc-1");
        document.setSpoilerLevel("LOW");
        document.setVersion(1);
        return document;
    }
}
