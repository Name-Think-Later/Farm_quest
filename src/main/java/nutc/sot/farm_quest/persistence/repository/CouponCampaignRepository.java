package nutc.sot.farm_quest.persistence.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.CouponCampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponCampaignRepository extends JpaRepository<CouponCampaignEntity, UUID> {
    Optional<CouponCampaignEntity> findByCode(String code);
    Optional<CouponCampaignEntity> findByQuest_Id(UUID questId);
    Optional<CouponCampaignEntity> findByGame_IdAndCode(UUID gameId, String code);

    @Query("""
            select campaign
            from CouponCampaignEntity campaign
            where campaign.quest.id = :questId
              and campaign.status = 'ACTIVE'
              and (campaign.validFrom is null or campaign.validFrom <= :time)
              and (campaign.validUntil is null or campaign.validUntil >= :time)
            """)
    Optional<CouponCampaignEntity> findActiveByQuestId(@Param("questId") UUID questId, @Param("time") OffsetDateTime time);
}
