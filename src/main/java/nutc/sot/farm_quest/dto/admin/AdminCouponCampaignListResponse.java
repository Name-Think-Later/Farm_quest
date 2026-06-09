package nutc.sot.farm_quest.dto.admin;

import java.util.List;

public record AdminCouponCampaignListResponse(
        List<AdminCouponCampaignResponse> campaigns
) {
}
