package nutc.sot.farm_quest.dto.quest;

import java.util.List;
import java.util.UUID;

public record AiRiddleConversationResponse(
        UUID questId,
        UUID conversationId,
        String status,
        boolean questCompleted,
        String nextStep,
        List<AiRiddleMessageItem> messages
) {
}
