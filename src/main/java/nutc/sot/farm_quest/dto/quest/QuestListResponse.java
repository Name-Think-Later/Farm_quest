package nutc.sot.farm_quest.dto.quest;

import java.util.List;

public record QuestListResponse(
        List<QuestDetailResponse> quests
) {
}
