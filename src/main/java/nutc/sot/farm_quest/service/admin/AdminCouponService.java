package nutc.sot.farm_quest.service.admin;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.admin.AdminCouponCampaignListResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponCampaignRequest;
import nutc.sot.farm_quest.dto.admin.AdminCouponCampaignResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponListResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponUsageListResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponUsageResponse;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.CouponCampaignEntity;
import nutc.sot.farm_quest.persistence.entity.CouponEntity;
import nutc.sot.farm_quest.persistence.entity.CouponUsageEntity;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.MerchantEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.repository.CouponCampaignRepository;
import nutc.sot.farm_quest.persistence.repository.CouponRepository;
import nutc.sot.farm_quest.persistence.repository.CouponUsageRepository;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import nutc.sot.farm_quest.persistence.repository.MerchantRepository;
import nutc.sot.farm_quest.persistence.repository.QuestRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminCouponService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "ACTIVE", "INACTIVE", "EXPIRED");

    private final CouponCampaignRepository couponCampaignRepository;
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final MerchantRepository merchantRepository;
    private final QuestRepository questRepository;
    private final GameRepository gameRepository;
    private final AuthProperties authProperties;

    @Transactional(readOnly = true)
    public AdminCouponCampaignListResponse getCouponCampaigns() {
        GameEntity game = getCurrentGame();
        return new AdminCouponCampaignListResponse(couponCampaignRepository.findByGame_IdOrderByUpdatedAtDesc(game.getId()).stream()
                .map(this::toCampaignResponse)
                .toList());
    }

    @Transactional
    public AdminCouponCampaignResponse createCouponCampaign(AdminCouponCampaignRequest request) {
        GameEntity game = getCurrentGame();
        QuestEntity quest = getQuestInCurrentGame(request.questId(), game.getId());
        MerchantEntity merchant = merchantRepository.findByGame_IdAndCode(game.getId(), normalizeRequired(request.merchantCode(), "merchantCode"))
                .orElseThrow(() -> new QuestException(QuestErrorCode.MERCHANT_NOT_FOUND, HttpStatus.NOT_FOUND, "Merchant not found"));

        if (request.validFrom() != null && request.validUntil() != null && request.validUntil().isBefore(request.validFrom())) {
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, "validUntil must be after validFrom");
        }

        OffsetDateTime now = OffsetDateTime.now();
        CouponCampaignEntity campaign = new CouponCampaignEntity();
        campaign.setId(UUID.randomUUID());
        campaign.setGame(game);
        campaign.setQuest(quest);
        campaign.setMerchant(merchant);
        campaign.setCode(normalizeRequired(request.code(), "code"));
        campaign.setTitle(normalizeRequired(request.title(), "title"));
        campaign.setDescription(StringUtils.hasText(request.description()) ? request.description().trim() : null);
        campaign.setStatus(normalizeStatus(request.status()));
        campaign.setValidFrom(request.validFrom());
        campaign.setValidUntil(request.validUntil());
        campaign.setCreatedAt(now);
        campaign.setUpdatedAt(now);

        try {
            return toCampaignResponse(couponCampaignRepository.save(campaign));
        } catch (DataIntegrityViolationException exception) {
            throw new QuestException(QuestErrorCode.ADMIN_RESOURCE_CONFLICT, HttpStatus.CONFLICT, "Coupon campaign code or quest already exists");
        }
    }

    @Transactional(readOnly = true)
    public AdminCouponListResponse getCoupons() {
        GameEntity game = getCurrentGame();
        return new AdminCouponListResponse(couponRepository.findByGame_IdOrderByIssuedAtDesc(game.getId()).stream()
                .map(this::toCouponResponse)
                .toList());
    }

    @Transactional(readOnly = true)
    public AdminCouponUsageListResponse getCouponUsages() {
        GameEntity game = getCurrentGame();
        return new AdminCouponUsageListResponse(couponUsageRepository.findByCoupon_CouponCampaign_Game_IdOrderByUsedAtDesc(game.getId()).stream()
                .map(this::toCouponUsageResponse)
                .toList());
    }

    private QuestEntity getQuestInCurrentGame(UUID questId, UUID gameId) {
        return questRepository.findById(questId)
                .filter(quest -> quest.getGame().getId().equals(gameId))
                .orElseThrow(() -> new QuestException(QuestErrorCode.QUEST_NOT_FOUND, HttpStatus.NOT_FOUND, "Quest not found"));
    }

    private GameEntity getCurrentGame() {
        String gameCode = authProperties.getGameCode();
        if (!StringUtils.hasText(gameCode)) {
            throw new QuestException(QuestErrorCode.GAME_NOT_FOUND, HttpStatus.SERVICE_UNAVAILABLE, "Game code is not configured");
        }
        return gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new QuestException(QuestErrorCode.GAME_NOT_FOUND, HttpStatus.NOT_FOUND, "Game not found"));
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "DRAFT";
        }
        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, "Coupon campaign status is invalid");
        }
        return normalizedStatus;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private AdminCouponCampaignResponse toCampaignResponse(CouponCampaignEntity campaign) {
        return new AdminCouponCampaignResponse(
                campaign.getId(),
                campaign.getQuest().getId(),
                campaign.getMerchant().getId(),
                campaign.getMerchant().getName(),
                campaign.getCode(),
                campaign.getTitle(),
                campaign.getDescription(),
                campaign.getStatus(),
                campaign.getValidFrom(),
                campaign.getValidUntil(),
                campaign.getUpdatedAt()
        );
    }

    private AdminCouponResponse toCouponResponse(CouponEntity coupon) {
        return new AdminCouponResponse(
                coupon.getId(),
                coupon.getVisitorAccount().getId(),
                coupon.getQuest().getId(),
                coupon.getCouponCampaign().getId(),
                coupon.getCouponCampaign().getTitle(),
                coupon.getStatus(),
                coupon.getDisplayCode(),
                coupon.getIssuedAt(),
                coupon.getExpiresAt(),
                coupon.getConsumedAt()
        );
    }

    private AdminCouponUsageResponse toCouponUsageResponse(CouponUsageEntity couponUsage) {
        return new AdminCouponUsageResponse(
                couponUsage.getId(),
                couponUsage.getCoupon().getId(),
                couponUsage.getVisitorAccount().getId(),
                couponUsage.getUsedAt(),
                couponUsage.getClientConfirmedAt(),
                couponUsage.getMetadata()
        );
    }
}
