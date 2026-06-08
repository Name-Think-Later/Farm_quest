package nutc.sot.farm_quest.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestProgressRepository extends JpaRepository<QuestProgressEntity, UUID> {
    Optional<QuestProgressEntity> findByVisitorAccount_IdAndQuest_Id(UUID visitorAccountId, UUID questId);
    List<QuestProgressEntity> findByVisitorAccount_IdOrderByUpdatedAtDesc(UUID visitorAccountId);
    Optional<QuestProgressEntity> findFirstByVisitorAccount_IdAndGame_IdAndStatusOrderByUpdatedAtDesc(UUID visitorAccountId, UUID gameId, String status);
    List<QuestProgressEntity> findByVisitorAccount_IdAndStatusInOrderByQuest_SortOrderAsc(UUID visitorAccountId, Collection<String> statuses);
}
