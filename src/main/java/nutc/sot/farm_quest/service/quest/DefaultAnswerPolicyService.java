package nutc.sot.farm_quest.service.quest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nutc.sot.farm_quest.dto.quest.AiRiddleMessageItem;
import nutc.sot.farm_quest.dto.quest.AiRiddleResult;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConfigEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DefaultAnswerPolicyService implements AnswerPolicyService {

    private static final Pattern VERDICT_PATTERN = Pattern.compile("(?mi)^VERDICT:\\s*(CORRECT|INCORRECT)\\s*$");
    private static final Pattern REASON_PATTERN = Pattern.compile("(?mi)^REASON:\\s*(.+)\\s*$");

    @Override
    public AiRiddleResult evaluate(AiRiddleConfigEntity config,
                                   String assistantReply,
                                   List<AiRiddleMessageItem> history,
                                   String visitorMessage,
                                   String judgeResponse,
                                   Map<String, Object> metadata) {
        JudgeDecision decision = parseJudgeDecision(judgeResponse);
        boolean correct = "CORRECT".equals(decision.verdict());
        boolean answerAttempt = correct || isAnswerAttempt(visitorMessage);

        Map<String, Object> enrichedMetadata = new HashMap<>(metadata);
        enrichedMetadata.put("judgeVerdict", decision.verdict());
        if (StringUtils.hasText(decision.reason())) {
            enrichedMetadata.put("judgeReason", decision.reason());
        }

        return new AiRiddleResult(
                StringUtils.hasText(assistantReply) ? assistantReply : "現在暫時無法提供回應，請稍後再試。",
                answerAttempt,
                correct,
                decision.verdict(),
                decision.reason(),
                enrichedMetadata
        );
    }

    private boolean isAnswerAttempt(String visitorMessage) {
        String normalized = visitorMessage.toLowerCase();
        return normalized.contains("答案") || normalized.contains("是") || normalized.contains("我覺得") || normalized.contains("應該");
    }

    private JudgeDecision parseJudgeDecision(String judgeResponse) {
        if (!StringUtils.hasText(judgeResponse)) {
            return new JudgeDecision("INCORRECT", "判定模型未提供有效結果。", false);
        }

        Matcher verdictMatcher = VERDICT_PATTERN.matcher(judgeResponse);
        Matcher reasonMatcher = REASON_PATTERN.matcher(judgeResponse);

        if (!verdictMatcher.find()) {
            return new JudgeDecision("INCORRECT", "判定模型輸出格式無法解析。", false);
        }

        String verdict = verdictMatcher.group(1).trim();
        String reason = reasonMatcher.find() ? reasonMatcher.group(1).trim() : "判定模型未提供原因。";
        return new JudgeDecision(verdict, reason, true);
    }

    private record JudgeDecision(String verdict, String reason, boolean parsed) {
    }
}
