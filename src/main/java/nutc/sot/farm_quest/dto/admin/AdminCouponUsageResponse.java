package nutc.sot.farm_quest.dto.admin;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record AdminCouponUsageResponse(
        UUID couponUsageId,
        UUID couponId,
        UUID visitorAccountId,
        OffsetDateTime usedAt,
        OffsetDateTime clientConfirmedAt,
        Map<String, Object> metadata
) {
}
