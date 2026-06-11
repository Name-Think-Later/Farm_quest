package nutc.sot.farm_quest.service.coupon;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.coupon.CouponDetailResponse;
import nutc.sot.farm_quest.dto.coupon.CouponListResponse;
import nutc.sot.farm_quest.dto.coupon.CouponSummary;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.CouponCampaignEntity;
import nutc.sot.farm_quest.persistence.entity.CouponEntity;
import nutc.sot.farm_quest.persistence.entity.MerchantEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.persistence.repository.CouponCampaignRepository;
import nutc.sot.farm_quest.persistence.repository.CouponRepository;
import nutc.sot.farm_quest.service.auth.SessionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private static final String STATUS_ISSUED = "ISSUED";
    private static final String STATUS_CONSUMED = "CONSUMED";
    private static final String STATUS_EXPIRED = "EXPIRED";

    private final SessionService sessionService;
    private final CouponCampaignRepository couponCampaignRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public CouponEntity issueCouponForCompletedQuest(VisitorAccountEntity visitorAccount, QuestEntity quest, OffsetDateTime issuedAt) {
        CouponCampaignEntity campaign = couponCampaignRepository
                .findActiveByQuestId(quest.getId(), issuedAt)
                .orElseThrow(() -> new QuestException(QuestErrorCode.COUPON_NOT_AVAILABLE, HttpStatus.BAD_REQUEST, "Coupon campaign is not available"));

        return couponRepository
                .findByVisitorAccount_IdAndCouponCampaign_Id(visitorAccount.getId(), campaign.getId())
                .orElseGet(() -> createCoupon(visitorAccount, quest, campaign, issuedAt));
    }

    @Transactional(readOnly = true)
    public CouponListResponse getMyCoupons(String token) {
        VisitorSessionEntity session = sessionService.requireActiveSession(token);
        List<CouponSummary> coupons = couponRepository.findByVisitorAccount_IdOrderByIssuedAtDesc(session.getVisitorAccount().getId())
                .stream()
                .map(this::toSummary)
                .toList();
        return new CouponListResponse(coupons);
    }

    @Transactional(readOnly = true)
    public CouponDetailResponse getCoupon(String token, UUID couponId) {
        VisitorSessionEntity session = sessionService.requireActiveSession(token);
        CouponEntity coupon = couponRepository.findByIdAndVisitorAccount_Id(couponId, session.getVisitorAccount().getId())
                .orElseThrow(() -> new QuestException(QuestErrorCode.COUPON_NOT_FOUND, HttpStatus.NOT_FOUND, "Coupon not found"));
        return toDetail(coupon);
    }

    private CouponEntity createCoupon(VisitorAccountEntity visitorAccount,
                                      QuestEntity quest,
                                      CouponCampaignEntity campaign,
                                      OffsetDateTime issuedAt) {
        CouponEntity coupon = new CouponEntity();
        coupon.setId(UUID.randomUUID());
        coupon.setGame(quest.getGame());
        coupon.setVisitorAccount(visitorAccount);
        coupon.setQuest(quest);
        coupon.setCouponCampaign(campaign);
        coupon.setStatus(STATUS_ISSUED);
        coupon.setIssuedAt(issuedAt);
        coupon.setExpiresAt(resolveExpiresAt(campaign, issuedAt));
        coupon.setDisplayCode(buildDisplayCode(campaign));
        coupon.setCreatedAt(issuedAt);
        coupon.setUpdatedAt(issuedAt);
        try {
            return couponRepository.save(coupon);
        } catch (DataIntegrityViolationException exception) {
            return couponRepository.findByVisitorAccount_IdAndCouponCampaign_Id(visitorAccount.getId(), campaign.getId())
                    .orElseThrow(() -> exception);
        }
    }

    private OffsetDateTime resolveExpiresAt(CouponCampaignEntity campaign, OffsetDateTime issuedAt) {
        if (campaign.getValidUntil() != null) {
            return campaign.getValidUntil();
        }
        return issuedAt.plusDays(7);
    }

    private String buildDisplayCode(CouponCampaignEntity campaign) {
        String prefix = campaign.getCode() == null ? "CPN" : campaign.getCode().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        String normalizedPrefix = prefix.length() > 8 ? prefix.substring(0, 8) : prefix;
        return normalizedPrefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private CouponSummary toSummary(CouponEntity coupon) {
        CouponCampaignEntity campaign = coupon.getCouponCampaign();
        MerchantEntity merchant = campaign.getMerchant();
        return new CouponSummary(
                coupon.getId(),
                coupon.getQuest().getId(),
                campaign.getId(),
                merchant != null ? merchant.getId() : null,
                campaign.getTitle(),
                merchant != null ? merchant.getName() : null,
                resolveStatus(coupon, OffsetDateTime.now()),
                coupon.getDisplayCode(),
                coupon.getIssuedAt(),
                coupon.getExpiresAt(),
                coupon.getConsumedAt()
        );
    }

    private CouponDetailResponse toDetail(CouponEntity coupon) {
        CouponCampaignEntity campaign = coupon.getCouponCampaign();
        MerchantEntity merchant = campaign.getMerchant();
        return new CouponDetailResponse(
                coupon.getId(),
                coupon.getQuest().getId(),
                campaign.getId(),
                merchant != null ? merchant.getId() : null,
                campaign.getTitle(),
                campaign.getDescription(),
                merchant != null ? merchant.getName() : null,
                merchant != null ? merchant.getAddress() : null,
                resolveStatus(coupon, OffsetDateTime.now()),
                coupon.getDisplayCode(),
                coupon.getIssuedAt(),
                coupon.getExpiresAt(),
                coupon.getConsumedAt()
        );
    }

    private String resolveStatus(CouponEntity coupon, OffsetDateTime now) {
        if (STATUS_CONSUMED.equals(coupon.getStatus())) {
            return STATUS_CONSUMED;
        }
        if (coupon.getExpiresAt().isBefore(now)) {
            return STATUS_EXPIRED;
        }
        return coupon.getStatus();
    }
}
