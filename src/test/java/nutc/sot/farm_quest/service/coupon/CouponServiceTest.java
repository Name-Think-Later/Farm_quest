package nutc.sot.farm_quest.service.coupon;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.CouponCampaignEntity;
import nutc.sot.farm_quest.persistence.entity.CouponEntity;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.MerchantEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.persistence.repository.CouponCampaignRepository;
import nutc.sot.farm_quest.persistence.repository.CouponRepository;
import nutc.sot.farm_quest.service.auth.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CouponServiceTest {

    private final SessionService sessionService = mock(SessionService.class);
    private final CouponCampaignRepository couponCampaignRepository = mock(CouponCampaignRepository.class);
    private final CouponRepository couponRepository = mock(CouponRepository.class);
    private final CouponService couponService = new CouponService(sessionService, couponCampaignRepository, couponRepository);

    @Test
    void issueCouponForCompletedQuestCreatesCouponWhenMissing() {
        VisitorAccountEntity visitorAccount = visitorAccount();
        QuestEntity quest = quest();
        CouponCampaignEntity campaign = campaign();
        OffsetDateTime issuedAt = OffsetDateTime.parse("2026-06-09T10:00:00+08:00");

        when(couponCampaignRepository.findActiveByQuestId(quest.getId(), issuedAt)).thenReturn(Optional.of(campaign));
        when(couponRepository.findByVisitorAccount_IdAndCouponCampaign_Id(visitorAccount.getId(), campaign.getId())).thenReturn(Optional.empty());
        when(couponRepository.save(any(CouponEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CouponEntity coupon = couponService.issueCouponForCompletedQuest(visitorAccount, quest, issuedAt);

        assertThat(coupon.getStatus()).isEqualTo("ISSUED");
        assertThat(coupon.getQuest()).isEqualTo(quest);
        assertThat(coupon.getCouponCampaign()).isEqualTo(campaign);
        assertThat(coupon.getDisplayCode()).isNotBlank();
    }

    @Test
    void issueCouponForCompletedQuestReturnsExistingCouponWhenAlreadyIssued() {
        VisitorAccountEntity visitorAccount = visitorAccount();
        QuestEntity quest = quest();
        CouponCampaignEntity campaign = campaign();
        CouponEntity existingCoupon = coupon(visitorAccount, quest, campaign);
        OffsetDateTime issuedAt = OffsetDateTime.parse("2026-06-09T10:00:00+08:00");

        when(couponCampaignRepository.findActiveByQuestId(quest.getId(), issuedAt)).thenReturn(Optional.of(campaign));
        when(couponRepository.findByVisitorAccount_IdAndCouponCampaign_Id(visitorAccount.getId(), campaign.getId())).thenReturn(Optional.of(existingCoupon));

        CouponEntity coupon = couponService.issueCouponForCompletedQuest(visitorAccount, quest, issuedAt);

        assertThat(coupon).isEqualTo(existingCoupon);
    }

    @Test
    void issueCouponForCompletedQuestRecoversFromUniqueConstraintRace() {
        VisitorAccountEntity visitorAccount = visitorAccount();
        QuestEntity quest = quest();
        CouponCampaignEntity campaign = campaign();
        CouponEntity existingCoupon = coupon(visitorAccount, quest, campaign);
        OffsetDateTime issuedAt = OffsetDateTime.parse("2026-06-09T10:00:00+08:00");

        when(couponCampaignRepository.findActiveByQuestId(quest.getId(), issuedAt)).thenReturn(Optional.of(campaign));
        when(couponRepository.findByVisitorAccount_IdAndCouponCampaign_Id(visitorAccount.getId(), campaign.getId()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingCoupon));
        when(couponRepository.save(any(CouponEntity.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        CouponEntity coupon = couponService.issueCouponForCompletedQuest(visitorAccount, quest, issuedAt);

        assertThat(coupon).isEqualTo(existingCoupon);
    }

    @Test
    void getCouponReturnsOnlyOwnedCoupon() {
        VisitorSessionEntity session = session(visitorAccount());
        CouponEntity coupon = coupon(session.getVisitorAccount(), quest(), campaign());
        when(sessionService.requireActiveSession("session-token")).thenReturn(session);
        when(couponRepository.findByIdAndVisitorAccount_Id(coupon.getId(), session.getVisitorAccount().getId())).thenReturn(Optional.of(coupon));

        var response = couponService.getCoupon("session-token", coupon.getId());

        assertThat(response.couponId()).isEqualTo(coupon.getId());
        assertThat(response.merchantName()).isEqualTo("春茶小舖");
    }

    private VisitorSessionEntity session(VisitorAccountEntity visitorAccount) {
        VisitorSessionEntity session = new VisitorSessionEntity();
        session.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        session.setVisitorAccount(visitorAccount);
        session.setGame(visitorAccount.getGame());
        return session;
    }

    private VisitorAccountEntity visitorAccount() {
        VisitorAccountEntity visitorAccount = new VisitorAccountEntity();
        visitorAccount.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        visitorAccount.setGame(game());
        return visitorAccount;
    }

    private QuestEntity quest() {
        QuestEntity quest = new QuestEntity();
        quest.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        quest.setGame(game());
        return quest;
    }

    private CouponCampaignEntity campaign() {
        CouponCampaignEntity campaign = new CouponCampaignEntity();
        campaign.setId(UUID.fromString("55555555-5555-5555-5555-555555555555"));
        campaign.setGame(game());
        campaign.setQuest(quest());
        campaign.setMerchant(merchant());
        campaign.setCode("tea-coupon-01");
        campaign.setTitle("茶香折扣券");
        campaign.setDescription("完成任務後可折抵 50 元");
        campaign.setStatus("ACTIVE");
        campaign.setValidUntil(OffsetDateTime.parse("2026-06-16T10:00:00+08:00"));
        return campaign;
    }

    private CouponEntity coupon(VisitorAccountEntity visitorAccount, QuestEntity quest, CouponCampaignEntity campaign) {
        CouponEntity coupon = new CouponEntity();
        coupon.setId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        coupon.setGame(quest.getGame());
        coupon.setVisitorAccount(visitorAccount);
        coupon.setQuest(quest);
        coupon.setCouponCampaign(campaign);
        coupon.setStatus("ISSUED");
        coupon.setIssuedAt(OffsetDateTime.parse("2026-06-09T10:00:00+08:00"));
        coupon.setExpiresAt(OffsetDateTime.parse("2026-06-16T10:00:00+08:00"));
        coupon.setDisplayCode("TEACOUP-ABCD1234");
        return coupon;
    }

    private MerchantEntity merchant() {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
        merchant.setGame(game());
        merchant.setName("春茶小舖");
        merchant.setAddress("南投縣名間鄉茶園路 1 號");
        return merchant;
    }

    private GameEntity game() {
        GameEntity game = new GameEntity();
        game.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        return game;
    }
}
