package nutc.sot.farm_quest.persistence.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {
    boolean existsByVisitorAccount_IdAndCouponCampaign_Id(UUID visitorAccountId, UUID couponCampaignId);
    Optional<CouponEntity> findByVisitorAccount_IdAndCouponCampaign_Id(UUID visitorAccountId, UUID couponCampaignId);
    List<CouponEntity> findByVisitorAccount_IdOrderByIssuedAtDesc(UUID visitorAccountId);
    Optional<CouponEntity> findByIdAndVisitorAccount_Id(UUID id, UUID visitorAccountId);
    List<CouponEntity> findByVisitorAccount_IdAndStatusAndExpiresAtAfterOrderByIssuedAtDesc(UUID visitorAccountId, String status, OffsetDateTime now);
    List<CouponEntity> findByGame_IdOrderByIssuedAtDesc(UUID gameId);
    List<CouponEntity> findByGame_IdAndStatusOrderByIssuedAtDesc(UUID gameId, String status);
    List<CouponEntity> findByQuest_IdOrderByIssuedAtDesc(UUID questId);

    @Modifying
    @Query("""
            update CouponEntity coupon
               set coupon.status = :toStatus,
                   coupon.consumedAt = :time,
                   coupon.updatedAt = :time
             where coupon.id = :couponId
               and coupon.visitorAccount.id = :visitorAccountId
               and coupon.status = :fromStatus
               and coupon.consumedAt is null
               and coupon.expiresAt > :time
            """)
    int consumeCoupon(@Param("couponId") UUID couponId,
                      @Param("visitorAccountId") UUID visitorAccountId,
                      @Param("fromStatus") String fromStatus,
                      @Param("toStatus") String toStatus,
                      @Param("time") OffsetDateTime time);
}
