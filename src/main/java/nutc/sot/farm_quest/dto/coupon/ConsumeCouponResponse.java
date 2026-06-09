package nutc.sot.farm_quest.dto.coupon;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsumeCouponResponse(
        UUID couponId,
        UUID couponUsageId,
        String status,
        OffsetDateTime consumedAt
) {
}
