package nutc.sot.farm_quest.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestRepository extends JpaRepository<QuestEntity, UUID> {
    Optional<QuestEntity> findByGame_IdAndCode(UUID gameId, String code);
    List<QuestEntity> findByStatusOrderBySortOrderAsc(String status);
    List<QuestEntity> findByGame_IdOrderBySortOrderAsc(UUID gameId);
    List<QuestEntity> findByGame_IdAndStatusOrderBySortOrderAsc(UUID gameId, String status);
    List<QuestEntity> findByGame_IdAndStatusInOrderBySortOrderAsc(UUID gameId, List<String> statuses);
}
