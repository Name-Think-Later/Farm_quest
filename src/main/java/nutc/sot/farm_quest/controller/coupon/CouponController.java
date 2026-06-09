package nutc.sot.farm_quest.controller.coupon;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.coupon.ConsumeCouponRequest;
import nutc.sot.farm_quest.dto.coupon.ConsumeCouponResponse;
import nutc.sot.farm_quest.dto.coupon.CouponDetailResponse;
import nutc.sot.farm_quest.dto.coupon.CouponListResponse;
import nutc.sot.farm_quest.service.coupon.CouponService;
import nutc.sot.farm_quest.service.coupon.CouponUsageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CouponUsageService couponUsageService;

    @GetMapping("/my")
    public ResponseEntity<CouponListResponse> getMyCoupons(HttpServletRequest request) {
        return ResponseEntity.ok(couponService.getMyCoupons(extractBearerToken(request)));
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<CouponDetailResponse> getCoupon(@PathVariable UUID couponId, HttpServletRequest request) {
        return ResponseEntity.ok(couponService.getCoupon(extractBearerToken(request), couponId));
    }

    @PostMapping("/{couponId}/consume")
    public ResponseEntity<ConsumeCouponResponse> consumeCoupon(@PathVariable UUID couponId,
                                                               @RequestBody(required = false) ConsumeCouponRequest request,
                                                               HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(couponUsageService.consumeCoupon(extractBearerToken(httpServletRequest), couponId, request));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
