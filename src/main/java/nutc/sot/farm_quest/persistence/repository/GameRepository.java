package nutc.sot.farm_quest.persistence.repository;

import nutc.sot.farm_quest.persistence.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<GameEntity, UUID> {
    Optional<GameEntity> findByCode(String code);
}
