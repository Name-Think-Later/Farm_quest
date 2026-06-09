package nutc.sot.farm_quest.dto.coupon;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CouponSummary(
        UUID couponId,
        UUID questId,
        UUID couponCampaignId,
        UUID merchantId,
        String title,
        String merchantName,
        String status,
        String displayCode,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime consumedAt
) {
}
