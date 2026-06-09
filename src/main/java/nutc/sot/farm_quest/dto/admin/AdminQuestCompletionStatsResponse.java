package nutc.sot.farm_quest.dto.admin;

import java.util.UUID;

public record AdminQuestCompletionStatsResponse(
        UUID questId,
        String questCode,
        String questTitle,
        long completedQuestCount
) {
}
