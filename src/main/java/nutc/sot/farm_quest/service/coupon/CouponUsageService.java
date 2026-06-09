package nutc.sot.farm_quest.service.coupon;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.coupon.ConsumeCouponRequest;
import nutc.sot.farm_quest.dto.coupon.ConsumeCouponResponse;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.CouponEntity;
import nutc.sot.farm_quest.persistence.entity.CouponUsageEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.persistence.repository.CouponRepository;
import nutc.sot.farm_quest.persistence.repository.CouponUsageRepository;
import nutc.sot.farm_quest.service.auth.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponUsageService {

    private static final String STATUS_ISSUED = "ISSUED";
    private static final String STATUS_CONSUMED = "CONSUMED";

    private final SessionService sessionService;
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Transactional
    public ConsumeCouponResponse consumeCoupon(String token, UUID couponId, ConsumeCouponRequest request) {
        VisitorSessionEntity session = sessionService.requireActiveSession(token);
        OffsetDateTime now = OffsetDateTime.now();
        CouponEntity coupon = couponRepository.findByIdAndVisitorAccount_Id(couponId, session.getVisitorAccount().getId())
                .orElseThrow(() -> new QuestException(QuestErrorCode.COUPON_NOT_FOUND, HttpStatus.NOT_FOUND, "Coupon not found"));

        validateCouponForConsume(coupon, now);

        int updatedRows = couponRepository.consumeCoupon(couponId, session.getVisitorAccount().getId(), STATUS_ISSUED, STATUS_CONSUMED, now);
        if (updatedRows == 0) {
            CouponEntity latestCoupon = couponRepository.findByIdAndVisitorAccount_Id(couponId, session.getVisitorAccount().getId())
                    .orElseThrow(() -> new QuestException(QuestErrorCode.COUPON_NOT_FOUND, HttpStatus.NOT_FOUND, "Coupon not found"));
            validateCouponForConsume(latestCoupon, now);
            throw new QuestException(QuestErrorCode.COUPON_NOT_AVAILABLE, HttpStatus.CONFLICT, "Coupon is not available");
        }

        CouponUsageEntity usage = new CouponUsageEntity();
        usage.setId(UUID.randomUUID());
        usage.setCoupon(coupon);
        usage.setVisitorAccount(session.getVisitorAccount());
        usage.setUsedAt(now);
        usage.setClientConfirmedAt(request == null ? null : request.clientConfirmedAt());
        usage.setMetadata(request == null || request.metadata() == null ? Map.of() : request.metadata());
        usage.setCreatedAt(now);
        CouponUsageEntity savedUsage = couponUsageRepository.save(usage);

        return new ConsumeCouponResponse(couponId, savedUsage.getId(), STATUS_CONSUMED, now);
    }

    private void validateCouponForConsume(CouponEntity coupon, OffsetDateTime now) {
        if (coupon.getExpiresAt().isBefore(now)) {
            throw new QuestException(QuestErrorCode.COUPON_EXPIRED, HttpStatus.BAD_REQUEST, "Coupon has expired");
        }
        if (STATUS_CONSUMED.equals(coupon.getStatus()) || coupon.getConsumedAt() != null || couponUsageRepository.existsByCoupon_Id(coupon.getId())) {
            throw new QuestException(QuestErrorCode.COUPON_ALREADY_CONSUMED, HttpStatus.CONFLICT, "Coupon has already been consumed");
        }
        if (!STATUS_ISSUED.equals(coupon.getStatus())) {
            throw new QuestException(QuestErrorCode.COUPON_NOT_AVAILABLE, HttpStatus.BAD_REQUEST, "Coupon is not available");
        }
    }
}
