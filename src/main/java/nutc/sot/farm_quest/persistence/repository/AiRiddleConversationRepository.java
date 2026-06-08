package nutc.sot.farm_quest.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRiddleConversationRepository extends JpaRepository<AiRiddleConversationEntity, UUID> {
    Optional<AiRiddleConversationEntity> findFirstByVisitorAccount_IdAndQuest_IdAndStatusOrderByStartedAtDesc(UUID visitorAccountId, UUID questId, String status);
    Optional<AiRiddleConversationEntity> findFirstByVisitorAccount_IdAndQuest_IdOrderByStartedAtDesc(UUID visitorAccountId, UUID questId);
    Optional<AiRiddleConversationEntity> findByIdAndVisitorAccount_Id(UUID id, UUID visitorAccountId);
}
