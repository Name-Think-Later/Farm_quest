package nutc.sot.farm_quest.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminAiRiddleConfigResponse(
        UUID questId,
        String questCode,
        String questTitle,
        String riddlePrompt,
        String answerCriteria,
        String spoilerPolicy,
        String completionPolicy,
        String status,
        OffsetDateTime updatedAt
) {
}
