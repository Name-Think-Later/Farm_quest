package nutc.sot.farm_quest.dto.coupon;

import java.util.List;

public record CouponListResponse(
        List<CouponSummary> coupons
) {
}
