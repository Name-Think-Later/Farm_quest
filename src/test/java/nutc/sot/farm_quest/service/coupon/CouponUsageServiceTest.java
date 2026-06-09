package nutc.sot.farm_quest.service.coupon;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.dto.coupon.ConsumeCouponRequest;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.CouponEntity;
import nutc.sot.farm_quest.persistence.entity.CouponUsageEntity;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.persistence.repository.CouponRepository;
import nutc.sot.farm_quest.persistence.repository.CouponUsageRepository;
import nutc.sot.farm_quest.service.auth.SessionService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CouponUsageServiceTest {

    private final SessionService sessionService = mock(SessionService.class);
    private final CouponRepository couponRepository = mock(CouponRepository.class);
    private final CouponUsageRepository couponUsageRepository = mock(CouponUsageRepository.class);
    private final CouponUsageService couponUsageService = new CouponUsageService(sessionService, couponRepository, couponUsageRepository);

    @Test
    void consumeCouponConsumesIssuedCoupon() {
        VisitorSessionEntity session = session();
        CouponEntity coupon = coupon("ISSUED");
        UUID couponId = coupon.getId();

        when(sessionService.requireActiveSession("session-token")).thenReturn(session);
        when(couponRepository.findByIdAndVisitorAccount_Id(couponId, session.getVisitorAccount().getId())).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByCoupon_Id(couponId)).thenReturn(false);
        when(couponRepository.consumeCoupon(eq(couponId), eq(session.getVisitorAccount().getId()), eq("ISSUED"), eq("CONSUMED"), any())).thenReturn(1);
        when(couponUsageRepository.save(any(CouponUsageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = couponUsageService.consumeCoupon(
                "session-token",
                couponId,
                new ConsumeCouponRequest(OffsetDateTime.parse("2026-06-09T10:05:00+08:00"), Map.of("source", "visitor"))
        );

        assertThat(response.couponId()).isEqualTo(couponId);
        assertThat(response.status()).isEqualTo("CONSUMED");
    }

    @Test
    void consumeCouponRejectsExpiredCoupon() {
        VisitorSessionEntity session = session();
        CouponEntity coupon = coupon("ISSUED");
        coupon.setExpiresAt(OffsetDateTime.now().minusMinutes(1));

        when(sessionService.requireActiveSession("session-token")).thenReturn(session);
        when(couponRepository.findByIdAndVisitorAccount_Id(coupon.getId(), session.getVisitorAccount().getId())).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponUsageService.consumeCoupon("session-token", coupon.getId(), null))
                .isInstanceOf(QuestException.class)
                .extracting(exception -> ((QuestException) exception).getErrorCode())
                .isEqualTo(QuestErrorCode.COUPON_EXPIRED);
    }

    @Test
    void consumeCouponRejectsAlreadyConsumedCoupon() {
        VisitorSessionEntity session = session();
        CouponEntity coupon = coupon("CONSUMED");
        coupon.setConsumedAt(OffsetDateTime.now().minusMinutes(1));

        when(sessionService.requireActiveSession("session-token")).thenReturn(session);
        when(couponRepository.findByIdAndVisitorAccount_Id(coupon.getId(), session.getVisitorAccount().getId())).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponUsageService.consumeCoupon("session-token", coupon.getId(), null))
                .isInstanceOf(QuestException.class)
                .extracting(exception -> ((QuestException) exception).getErrorCode())
                .isEqualTo(QuestErrorCode.COUPON_ALREADY_CONSUMED);
    }

    @Test
    void consumeCouponMapsRapidRepeatToAlreadyConsumed() {
        VisitorSessionEntity session = session();
        CouponEntity coupon = coupon("ISSUED");
        CouponEntity latestCoupon = coupon("CONSUMED");
        latestCoupon.setConsumedAt(OffsetDateTime.now());
        UUID couponId = coupon.getId();

        when(sessionService.requireActiveSession("session-token")).thenReturn(session);
        when(couponRepository.findByIdAndVisitorAccount_Id(couponId, session.getVisitorAccount().getId()))
                .thenReturn(Optional.of(coupon))
                .thenReturn(Optional.of(latestCoupon));
        when(couponUsageRepository.existsByCoupon_Id(couponId)).thenReturn(false);
        when(couponRepository.consumeCoupon(eq(couponId), eq(session.getVisitorAccount().getId()), eq("ISSUED"), eq("CONSUMED"), any())).thenReturn(0);

        assertThatThrownBy(() -> couponUsageService.consumeCoupon("session-token", couponId, null))
                .isInstanceOf(QuestException.class)
                .extracting(exception -> ((QuestException) exception).getErrorCode())
                .isEqualTo(QuestErrorCode.COUPON_ALREADY_CONSUMED);
    }

    private VisitorSessionEntity session() {
        VisitorAccountEntity visitorAccount = new VisitorAccountEntity();
        visitorAccount.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        visitorAccount.setGame(game());

        VisitorSessionEntity session = new VisitorSessionEntity();
        session.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        session.setVisitorAccount(visitorAccount);
        session.setGame(visitorAccount.getGame());
        return session;
    }

    private CouponEntity coupon(String status) {
        CouponEntity coupon = new CouponEntity();
        coupon.setId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        coupon.setGame(game());
        coupon.setQuest(quest());
        coupon.setVisitorAccount(session().getVisitorAccount());
        coupon.setStatus(status);
        coupon.setIssuedAt(OffsetDateTime.now().minusDays(1));
        coupon.setExpiresAt(OffsetDateTime.now().plusDays(1));
        return coupon;
    }

    private QuestEntity quest() {
        QuestEntity quest = new QuestEntity();
        quest.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        quest.setGame(game());
        return quest;
    }

    private GameEntity game() {
        GameEntity game = new GameEntity();
        game.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        return game;
    }
}
