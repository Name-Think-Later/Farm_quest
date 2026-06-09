package nutc.sot.farm_quest.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.CouponCampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponCampaignRepository extends JpaRepository<CouponCampaignEntity, UUID> {
    Optional<CouponCampaignEntity> findByCode(String code);
    Optional<CouponCampaignEntity> findByQuest_Id(UUID questId);
    Optional<CouponCampaignEntity> findByGame_IdAndCode(UUID gameId, String code);
}
