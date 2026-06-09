package nutc.sot.farm_quest.dto.quest;

import java.util.Map;

public record AiRiddleResult(
        String replyContent,
        boolean answerAttempt,
        boolean correct,
        Map<String, Object> metadata
) {
}
