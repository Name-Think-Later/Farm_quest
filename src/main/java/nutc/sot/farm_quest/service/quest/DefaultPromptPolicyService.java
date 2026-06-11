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
                              String visitorMessage,
                              boolean isStartTrigger) {
        String historyText = formatHistory(history);
        String contextText = formatContext(documents);
        String systemPrompt = String.join("\n\n",
                "你是農遊解謎 AI 助手，負責像使用者提問謎題，因此不可以在使用者提問時直接給出、暗示、提及答案，回答時不可以有表情符號、格式化符號，以下是謎題資訊。",
                "任務名稱：" + quest.getTitle(),
                "猜謎規則：" + config.getRiddlePrompt(),
                "防爆雷規則：" + config.getSpoilerPolicy(),
                "完成規則：" + config.getCompletionPolicy(),
                "答案判定標準：" + config.getAnswerCriteria(),
                isStartTrigger
                        ? "這是使用者首次進入對話，請直接根據任務與知識文件出一個引導式問題或提示，不要等待使用者先發訊息。"
                        : "當使用者答對後，不需要再詢問任何需求，僅需恭喜使用者",
                contextText.isBlank() ? "目前沒有可用的 RAG 文件。" : "可用知識文件：\n" + contextText
        );
        String userPrompt = String.join("\n\n",
                historyText.isBlank() ? "尚無歷史對話。" : "歷史對話：\n" + historyText,
                isStartTrigger
                        ? "這是首次進場觸發，請以繁體中文出題或提供引導，讓使用者開始猜謎。"
                        : "使用者最新訊息：" + visitorMessage + "\n請以繁體中文回覆，提供提示或互動式引導，不要直接爆雷。"
        );
        return new PromptBundle(systemPrompt, userPrompt);
    }

    @Override
    public PromptBundle buildJudgePrompt(AiRiddleConfigEntity config,
                                         QuestEntity quest,
                                         QuestProgressEntity progress,
                                         List<AiRiddleMessageItem> history,
                                         List<Document> documents,
                                         String visitorMessage,
                                         String assistantReply) {
        String historyText = formatHistory(history);
        String contextText = formatContext(documents);
        String systemPrompt = String.join("\n\n",
                "你是農遊解謎答案判定器。",
                "請根據任務規則、答案判定標準、知識文件與對話內容，判斷使用者最新答案是否應視為答對。",
                "重要判斷規則：",
                "- 必須仔細分析 AI 助手剛剛的回覆內容。",
                "- 若 AI 助手明確表示「還缺」、「還不完整」、「很接近但還需要」、「不完全正確」等語句，必須判為 INCORRECT。",
                "- 若 AI 助手表示「答對了」、「完全正確」、「恭喜」或明確肯定答案，才可判為 CORRECT。",
                "- 若使用者是否定、反問、猜測未定案、只是在索取提示，必須判為 INCORRECT。",
                "- 若答案語意完全符合答案判定標準的核心要素，且 AI 助手沒有表示還需要補充，才可判為 CORRECT。",
                "不得輸出任何額外說明，只能輸出兩行。",
                "第一行格式：VERDICT: CORRECT 或 VERDICT: INCORRECT",
                "第二行格式：REASON: 一句簡短中文原因",
                "任務名稱：" + quest.getTitle(),
                "猜謎規則：" + config.getRiddlePrompt(),
                "完成規則：" + config.getCompletionPolicy(),
                "答案判定標準：" + config.getAnswerCriteria(),
                contextText.isBlank() ? "目前沒有可用的 RAG 文件。" : "可用知識文件：\n" + contextText
        );
        String userPrompt = String.join("\n\n",
                historyText.isBlank() ? "尚無歷史對話。" : "歷史對話：\n" + historyText,
                "AI 助手剛剛的回覆：" + assistantReply,
                "使用者最新訊息：" + visitorMessage
        );
        return new PromptBundle(systemPrompt, userPrompt);
    }

    private String formatHistory(List<AiRiddleMessageItem> history) {
        return history.stream()
                .map(item -> item.role() + ": " + item.content())
                .collect(Collectors.joining("\n"));
    }

    private String formatContext(List<Document> documents) {
        return documents.stream()
                .map(this::formatDocumentContext)
                .collect(Collectors.joining("\n"));
    }

    private String formatDocumentContext(Document document) {
        Object title = document.getMetadata().get("title");
        Object source = document.getMetadata().get("source");
        return "- 標題：" + (title == null ? "未命名" : title)
                + "；來源：" + (source == null ? "未知" : source)
                + "；內容：" + document.getText();
    }
}
