package nutc.sot.farm_quest.persistence.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {
    boolean existsByVisitorAccount_IdAndCouponCampaign_Id(UUID visitorAccountId, UUID couponCampaignId);
    Optional<CouponEntity> findByVisitorAccount_IdAndCouponCampaign_Id(UUID visitorAccountId, UUID couponCampaignId);
    List<CouponEntity> findByVisitorAccount_IdOrderByIssuedAtDesc(UUID visitorAccountId);
    Optional<CouponEntity> findByIdAndVisitorAccount_Id(UUID id, UUID visitorAccountId);
    List<CouponEntity> findByVisitorAccount_IdAndStatusAndExpiresAtAfterOrderByIssuedAtDesc(UUID visitorAccountId, String status, OffsetDateTime now);
}
