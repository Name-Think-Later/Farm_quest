package nutc.sot.farm_quest.dto.admin;

import java.util.UUID;

public record AdminCouponCampaignStatsResponse(
        UUID campaignId,
        UUID questId,
        String campaignCode,
        String campaignTitle,
        long issuedCouponCount,
        long usedCouponCount,
        double usageRate
) {
}
