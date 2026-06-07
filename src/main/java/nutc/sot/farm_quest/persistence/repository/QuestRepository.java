package nutc.sot.farm_quest.persistence.repository;

import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestRepository extends JpaRepository<QuestEntity, UUID> {
    List<QuestEntity> findByStatusOrderBySortOrderAsc(String status);
}
