package nutc.sot.farm_quest.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.CouponUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsageEntity, UUID> {
    boolean existsByCoupon_Id(UUID couponId);
    Optional<CouponUsageEntity> findByCoupon_Id(UUID couponId);
    List<CouponUsageEntity> findByVisitorAccount_IdOrderByUsedAtDesc(UUID visitorAccountId);
    List<CouponUsageEntity> findByCoupon_CouponCampaign_Game_IdOrderByUsedAtDesc(UUID gameId);
    List<CouponUsageEntity> findByCoupon_Quest_IdOrderByUsedAtDesc(UUID questId);
}
