package nutc.sot.farm_quest.dto.quest;

import java.time.OffsetDateTime;
import java.util.UUID;

public record QuestDetailResponse(
        UUID questId,
        String title,
        String description,
        int sortOrder,
        String status,
        OffsetDateTime startedAt,
        OffsetDateTime locationVerifiedAt,
        boolean current,
        String nextStep
) {
}
