package nutc.sot.farm_quest.service.admin;

import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.admin.AdminCouponCampaignStatsListResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponCampaignStatsResponse;
import nutc.sot.farm_quest.dto.admin.AdminOverviewStatsResponse;
import nutc.sot.farm_quest.dto.admin.AdminQuestCompletionStatsListResponse;
import nutc.sot.farm_quest.dto.admin.AdminQuestCompletionStatsResponse;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.repository.AdminStatisticsRepository;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final AdminStatisticsRepository adminStatisticsRepository;
    private final GameRepository gameRepository;
    private final AuthProperties authProperties;

    @Transactional(readOnly = true)
    public AdminOverviewStatsResponse getOverviewStats() {
        GameEntity game = getCurrentGame();
        AdminStatisticsRepository.OverviewStatisticsProjection stats = adminStatisticsRepository.summarizeByGameId(game.getId());
        return new AdminOverviewStatsResponse(
                stats.getCompletedQuestCount(),
                stats.getIssuedCouponCount(),
                stats.getUsedCouponCount(),
                stats.getUsageRate()
        );
    }

    @Transactional(readOnly = true)
    public AdminQuestCompletionStatsListResponse getQuestCompletionStats() {
        GameEntity game = getCurrentGame();
        return new AdminQuestCompletionStatsListResponse(adminStatisticsRepository.summarizeQuestCompletionByGameId(game.getId()).stream()
                .map(stat -> new AdminQuestCompletionStatsResponse(
                        stat.getQuestId(),
                        stat.getQuestCode(),
                        stat.getQuestTitle(),
                        stat.getCompletedQuestCount()
                ))
                .toList());
    }

    @Transactional(readOnly = true)
    public AdminCouponCampaignStatsListResponse getCouponCampaignStats() {
        GameEntity game = getCurrentGame();
        return new AdminCouponCampaignStatsListResponse(adminStatisticsRepository.summarizeCouponCampaignByGameId(game.getId()).stream()
                .map(stat -> new AdminCouponCampaignStatsResponse(
                        stat.getCampaignId(),
                        stat.getQuestId(),
                        stat.getCampaignCode(),
                        stat.getCampaignTitle(),
                        stat.getIssuedCouponCount(),
                        stat.getUsedCouponCount(),
                        stat.getUsageRate()
                ))
                .toList());
    }

    private GameEntity getCurrentGame() {
        String gameCode = authProperties.getGameCode();
        if (!StringUtils.hasText(gameCode)) {
            throw new QuestException(QuestErrorCode.GAME_NOT_FOUND, HttpStatus.SERVICE_UNAVAILABLE, "Game code is not configured");
        }
        return gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new QuestException(QuestErrorCode.GAME_NOT_FOUND, HttpStatus.NOT_FOUND, "Game not found"));
    }
}
