package nutc.sot.farm_quest.dto.quest;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiRiddleMessageItem(
        UUID messageId,
        String role,
        String content,
        Boolean answerCorrect,
        OffsetDateTime createdAt
) {
}
