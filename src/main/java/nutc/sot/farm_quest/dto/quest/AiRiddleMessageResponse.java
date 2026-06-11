package nutc.sot.farm_quest.dto.quest;

import java.util.UUID;

public record AiRiddleMessageResponse(
        UUID questId,
        UUID conversationId,
        String status,
        String replyContent,
        boolean correct,
        boolean questCompleted,
        String nextStep,
        String safeMessage,
        String judgeReason
) {
}
