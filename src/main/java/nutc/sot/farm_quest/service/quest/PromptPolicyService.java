package nutc.sot.farm_quest.service.quest;

import java.util.List;
import nutc.sot.farm_quest.dto.quest.AiRiddleMessageItem;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConfigEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import org.springframework.ai.document.Document;

public interface PromptPolicyService {

    PromptBundle build(AiRiddleConfigEntity config,
                       QuestEntity quest,
                       QuestProgressEntity progress,
                       List<AiRiddleMessageItem> history,
                       List<Document> documents,
                       String visitorMessage);

    record PromptBundle(String systemPrompt, String userPrompt) {
    }

    record PromptContext(QuestEntity quest, QuestProgressEntity progress) {
    }
}
