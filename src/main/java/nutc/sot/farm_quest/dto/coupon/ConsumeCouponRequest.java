package nutc.sot.farm_quest.dto.coupon;

import java.time.OffsetDateTime;
import java.util.Map;

public record ConsumeCouponRequest(
        OffsetDateTime clientConfirmedAt,
        Map<String, Object> metadata
) {
}
