package nutc.sot.farm_quest.dto.coupon;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CouponDetailResponse(
        UUID couponId,
        UUID questId,
        UUID couponCampaignId,
        String title,
        String description,
        String merchantName,
        String merchantAddress,
        String status,
        String displayCode,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime consumedAt
) {
}
