package nutc.sot.farm_quest.persistence.repository;

import java.util.List;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.AiRiddleMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRiddleMessageRepository extends JpaRepository<AiRiddleMessageEntity, UUID> {
    List<AiRiddleMessageEntity> findByConversation_IdOrderByCreatedAtAsc(UUID conversationId);
}
