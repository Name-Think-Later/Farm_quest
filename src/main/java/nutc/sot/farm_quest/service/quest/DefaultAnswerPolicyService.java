package nutc.sot.farm_quest.service.quest;

import java.util.List;
import java.util.Map;
import nutc.sot.farm_quest.dto.quest.AiRiddleMessageItem;
import nutc.sot.farm_quest.dto.quest.AiRiddleResult;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConfigEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DefaultAnswerPolicyService implements AnswerPolicyService {

    @Override
    public AiRiddleResult evaluate(AiRiddleConfigEntity config,
                                   String assistantReply,
                                   List<AiRiddleMessageItem> history,
                                   String visitorMessage,
                                   Map<String, Object> metadata) {
        boolean answerAttempt = isAnswerAttempt(visitorMessage);
        boolean correct = answerAttempt && isCorrect(visitorMessage, config.getAnswerCriteria());
        return new AiRiddleResult(
                StringUtils.hasText(assistantReply) ? assistantReply : "現在暫時無法提供回應，請稍後再試。",
                answerAttempt,
                correct,
                metadata
        );
    }

    private boolean isAnswerAttempt(String visitorMessage) {
        String normalized = visitorMessage.toLowerCase();
        return normalized.contains("答案") || normalized.contains("是") || normalized.contains("我覺得") || normalized.contains("應該");
    }

    private boolean isCorrect(String visitorMessage, String answerCriteria) {
        if (!StringUtils.hasText(answerCriteria)) {
            return false;
        }
        return visitorMessage.trim().equalsIgnoreCase(answerCriteria.trim())
                || visitorMessage.toLowerCase().contains(answerCriteria.trim().toLowerCase());
    }
}
