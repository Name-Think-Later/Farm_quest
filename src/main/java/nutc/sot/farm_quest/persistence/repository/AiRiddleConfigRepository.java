package nutc.sot.farm_quest.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRiddleConfigRepository extends JpaRepository<AiRiddleConfigEntity, UUID> {
    Optional<AiRiddleConfigEntity> findByQuest_Id(UUID questId);
    Optional<AiRiddleConfigEntity> findByQuest_IdAndStatus(UUID questId, String status);
}
