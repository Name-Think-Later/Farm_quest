package nutc.sot.farm_quest.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminCouponResponse(
        UUID couponId,
        UUID visitorAccountId,
        UUID questId,
        UUID couponCampaignId,
        String title,
        String status,
        String displayCode,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime consumedAt
) {
}
