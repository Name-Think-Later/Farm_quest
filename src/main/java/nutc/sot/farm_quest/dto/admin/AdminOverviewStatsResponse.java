package nutc.sot.farm_quest.dto.admin;

public record AdminOverviewStatsResponse(
        long completedQuestCount,
        long issuedCouponCount,
        long usedCouponCount,
        double usageRate
) {
}
