package nutc.sot.farm_quest.service.quest;

import java.util.List;
import java.util.stream.Collectors;
import nutc.sot.farm_quest.dto.quest.AiRiddleMessageItem;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConfigEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

@Service
public class DefaultPromptPolicyService implements PromptPolicyService {

    @Override
    public PromptBundle build(AiRiddleConfigEntity config,
                              QuestEntity quest,
                              QuestProgressEntity progress,
                              List<AiRiddleMessageItem> history,
                              List<Document> documents,
                              String visitorMessage) {
        String historyText = history.stream()
                .map(item -> item.role() + ": " + item.content())
                .collect(Collectors.joining("\n"));
        String contextText = documents.stream()
                .map(this::formatDocumentContext)
                .collect(Collectors.joining("\n"));
        String systemPrompt = String.join("\n\n",
                "你是農遊解謎 AI 助手。",
                "任務名稱：" + quest.getTitle(),
                "猜謎規則：" + config.getRiddlePrompt(),
                "防爆雷規則：" + config.getSpoilerPolicy(),
                "完成規則：" + config.getCompletionPolicy(),
                "答案判定標準：" + config.getAnswerCriteria(),
                "只有在使用者明確答對時，才可視為完成。",
                contextText.isBlank() ? "目前沒有可用的 RAG 文件。" : "可用知識文件：\n" + contextText
        );
        String userPrompt = String.join("\n\n",
                historyText.isBlank() ? "尚無歷史對話。" : "歷史對話：\n" + historyText,
                "使用者最新訊息：" + visitorMessage,
                "請以繁體中文回覆，提供提示或互動式引導，不要直接爆雷。"
        );
        return new PromptBundle(systemPrompt, userPrompt);
    }

    private String formatDocumentContext(Document document) {
        Object title = document.getMetadata().get("title");
        Object source = document.getMetadata().get("source");
        return "- 標題：" + (title == null ? "未命名" : title)
                + "；來源：" + (source == null ? "未知" : source)
                + "；內容：" + document.getText();
    }
}
