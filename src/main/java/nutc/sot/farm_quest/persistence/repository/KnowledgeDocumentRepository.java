package nutc.sot.farm_quest.persistence.repository;

import java.util.List;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocumentEntity, UUID> {
    List<KnowledgeDocumentEntity> findByEmbeddingStatus(String embeddingStatus);
    List<KnowledgeDocumentEntity> findByQuest_IdAndEmbeddingStatus(UUID questId, String embeddingStatus);
    List<KnowledgeDocumentEntity> findByLocation_IdAndEmbeddingStatus(UUID locationId, String embeddingStatus);
    List<KnowledgeDocumentEntity> findByQuest_IdAndSpoilerLevelAndEmbeddingStatus(UUID questId, String spoilerLevel, String embeddingStatus);
}
