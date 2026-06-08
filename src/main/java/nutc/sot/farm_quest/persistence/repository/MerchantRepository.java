package nutc.sot.farm_quest.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<MerchantEntity, UUID> {
    Optional<MerchantEntity> findByGame_IdAndCode(UUID gameId, String code);
}
