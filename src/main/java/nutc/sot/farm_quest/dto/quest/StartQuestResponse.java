package nutc.sot.farm_quest.dto.quest;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StartQuestResponse(
        UUID questId,
        String status,
        OffsetDateTime startedAt,
        String nextStep
) {
}
