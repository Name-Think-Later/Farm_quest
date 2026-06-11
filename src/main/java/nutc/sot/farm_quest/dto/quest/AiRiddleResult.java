package nutc.sot.farm_quest.dto.quest;

import java.util.Map;

public record AiRiddleResult(
        String replyContent,
        boolean answerAttempt,
        boolean correct,
        String judgeVerdict,
        String judgeReason,
        Map<String, Object> metadata
) {
}
