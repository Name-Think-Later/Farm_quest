package nutc.sot.farm_quest.persistence.repository;

import nutc.sot.farm_quest.persistence.entity.CouponCampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CouponCampaignRepository extends JpaRepository<CouponCampaignEntity, UUID> {
    Optional<CouponCampaignEntity> findByCode(String code);
}
