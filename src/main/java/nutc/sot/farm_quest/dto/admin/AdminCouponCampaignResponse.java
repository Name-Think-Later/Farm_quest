package nutc.sot.farm_quest.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminCouponCampaignResponse(
        UUID campaignId,
        UUID questId,
        UUID merchantId,
        String merchantName,
        String code,
        String title,
        String description,
        String status,
        OffsetDateTime validFrom,
        OffsetDateTime validUntil,
        OffsetDateTime updatedAt
) {
}
