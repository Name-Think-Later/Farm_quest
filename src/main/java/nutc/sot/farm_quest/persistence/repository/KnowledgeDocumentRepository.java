package nutc.sot.farm_quest.persistence.repository;

import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocumentEntity, UUID> {
    List<KnowledgeDocumentEntity> findByEmbeddingStatus(String embeddingStatus);
}
