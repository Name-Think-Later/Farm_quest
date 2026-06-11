package nutc.sot.farm_quest.service.quest;

import java.util.List;
import java.util.Map;
import nutc.sot.farm_quest.dto.quest.AiRiddleMessageItem;
import nutc.sot.farm_quest.dto.quest.AiRiddleResult;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConfigEntity;

public interface AnswerPolicyService {

    AiRiddleResult evaluate(AiRiddleConfigEntity config,
                            String assistantReply,
                            List<AiRiddleMessageItem> history,
                            String visitorMessage,
                            String judgeResponse,
                            Map<String, Object> metadata);
}
