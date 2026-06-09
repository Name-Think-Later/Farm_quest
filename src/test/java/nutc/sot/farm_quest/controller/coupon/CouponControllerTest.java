package nutc.sot.farm_quest.controller.coupon;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import nutc.sot.farm_quest.dto.coupon.ConsumeCouponResponse;
import nutc.sot.farm_quest.dto.coupon.CouponDetailResponse;
import nutc.sot.farm_quest.dto.coupon.CouponListResponse;
import nutc.sot.farm_quest.dto.coupon.CouponSummary;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import nutc.sot.farm_quest.exception.GlobalExceptionHandler;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.service.coupon.CouponService;
import nutc.sot.farm_quest.service.coupon.CouponUsageService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CouponControllerTest {

    private final CouponService couponService = mock(CouponService.class);
    private final CouponUsageService couponUsageService = mock(CouponUsageService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CouponController(couponService, couponUsageService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void getMyCouponsReturnsContract() throws Exception {
        when(couponService.getMyCoupons("session-token")).thenReturn(new CouponListResponse(List.of(
                new CouponSummary(
                        UUID.fromString("66666666-6666-6666-6666-666666666666"),
                        UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        UUID.fromString("55555555-5555-5555-5555-555555555555"),
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        "茶香折扣券",
                        "春茶小舖",
                        "ISSUED",
                        "TEACOUP-ABCD1234",
                        OffsetDateTime.parse("2026-06-09T10:00:00+08:00"),
                        OffsetDateTime.parse("2026-06-16T10:00:00+08:00"),
                        null
                )
        )));

        mockMvc.perform(get("/api/coupons/my")
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.coupons.length()").value(1))
                .andExpect(jsonPath("$.coupons[0].title").value("茶香折扣券"))
                .andExpect(jsonPath("$.coupons[0].merchantName").value("春茶小舖"));
    }

    @Test
    void getMyCouponsRejectsMissingSession() throws Exception {
        when(couponService.getMyCoupons(null))
                .thenThrow(new AuthException(AuthErrorCode.SESSION_INVALID, HttpStatus.UNAUTHORIZED, "Session token is invalid"));

        mockMvc.perform(get("/api/coupons/my"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SESSION_INVALID"));
    }

    @Test
    void getCouponReturnsContract() throws Exception {
        UUID couponId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        when(couponService.getCoupon("session-token", couponId)).thenReturn(new CouponDetailResponse(
                couponId,
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "茶香折扣券",
                "完成任務後可折抵 50 元",
                "春茶小舖",
                "南投縣名間鄉茶園路 1 號",
                "ISSUED",
                "TEACOUP-ABCD1234",
                OffsetDateTime.parse("2026-06-09T10:00:00+08:00"),
                OffsetDateTime.parse("2026-06-16T10:00:00+08:00"),
                null
        ));

        mockMvc.perform(get("/api/coupons/{couponId}", couponId)
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("茶香折扣券"))
                .andExpect(jsonPath("$.merchantAddress").value("南投縣名間鄉茶園路 1 號"));
    }

    @Test
    void getCouponReturnsNotFound() throws Exception {
        UUID couponId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        when(couponService.getCoupon("session-token", couponId))
                .thenThrow(new QuestException(QuestErrorCode.COUPON_NOT_FOUND, HttpStatus.NOT_FOUND, "Coupon not found"));

        mockMvc.perform(get("/api/coupons/{couponId}", couponId)
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COUPON_NOT_FOUND"));
    }

    @Test
    void consumeCouponReturnsContract() throws Exception {
        UUID couponId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        when(couponUsageService.consumeCoupon(eq("session-token"), eq(couponId), any()))
                .thenReturn(new ConsumeCouponResponse(
                        couponId,
                        UUID.fromString("77777777-7777-7777-7777-777777777777"),
                        "CONSUMED",
                        OffsetDateTime.parse("2026-06-09T10:05:00+08:00")
                ));

        mockMvc.perform(post("/api/coupons/{couponId}/consume", couponId)
                        .header("Authorization", "Bearer session-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientConfirmedAt":"2026-06-09T10:05:00+08:00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONSUMED"))
                .andExpect(jsonPath("$.couponUsageId").value("77777777-7777-7777-7777-777777777777"));
    }

    @Test
    void consumeCouponReturnsConsumedError() throws Exception {
        UUID couponId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        when(couponUsageService.consumeCoupon(eq("session-token"), eq(couponId), any()))
                .thenThrow(new QuestException(QuestErrorCode.COUPON_ALREADY_CONSUMED, HttpStatus.CONFLICT, "Coupon has already been consumed"));

        mockMvc.perform(post("/api/coupons/{couponId}/consume", couponId)
                        .header("Authorization", "Bearer session-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COUPON_ALREADY_CONSUMED"));
    }

    @Test
    void consumeCouponReturnsExpiredError() throws Exception {
        UUID couponId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        when(couponUsageService.consumeCoupon(eq("session-token"), eq(couponId), any()))
                .thenThrow(new QuestException(QuestErrorCode.COUPON_EXPIRED, HttpStatus.BAD_REQUEST, "Coupon has expired"));

        mockMvc.perform(post("/api/coupons/{couponId}/consume", couponId)
                        .header("Authorization", "Bearer session-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COUPON_EXPIRED"));
    }
}
