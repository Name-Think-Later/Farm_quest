package nutc.sot.farm_quest.service.quest;

import java.util.List;
import java.util.Map;
import nutc.sot.farm_quest.dto.quest.AiRiddleResult;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConfigEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAnswerPolicyServiceTest {

    private final DefaultAnswerPolicyService service = new DefaultAnswerPolicyService();

    @Test
    void evaluateMarksJudgeCorrectAnswerAsCorrect() {
        AiRiddleConfigEntity config = new AiRiddleConfigEntity();
        config.setAnswerCriteria("答案需包含茶葉");

        AiRiddleResult result = service.evaluate(
                config,
                "答對了",
                List.of(),
                "茶葉蛋",
                "VERDICT: CORRECT\nREASON: 使用者答案明確包含茶葉且符合題意。",
                Map.of()
        );

        assertThat(result.correct()).isTrue();
        assertThat(result.answerAttempt()).isTrue();
        assertThat(result.judgeVerdict()).isEqualTo("CORRECT");
        assertThat(result.judgeReason()).isEqualTo("使用者答案明確包含茶葉且符合題意。");
        assertThat(result.metadata()).containsEntry("judgeVerdict", "CORRECT");
    }

    @Test
    void evaluateKeepsHintRequestAsNotCorrect() {
        AiRiddleConfigEntity config = new AiRiddleConfigEntity();
        config.setAnswerCriteria("高山茶");

        AiRiddleResult result = service.evaluate(
                config,
                "提示內容",
                List.of(),
                "可以給我提示嗎",
                "VERDICT: INCORRECT\nREASON: 使用者是在索取提示，尚未正式作答。",
                Map.of()
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.answerAttempt()).isFalse();
        assertThat(result.judgeVerdict()).isEqualTo("INCORRECT");
    }

    @Test
    void evaluateTreatsMissingJudgeResponseAsIncorrect() {
        AiRiddleConfigEntity config = new AiRiddleConfigEntity();
        config.setAnswerCriteria("高山茶");

        AiRiddleResult result = service.evaluate(
                config,
                "提示內容",
                List.of(),
                "我覺得是紅茶",
                null,
                Map.of()
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.answerAttempt()).isTrue();
        assertThat(result.judgeVerdict()).isEqualTo("INCORRECT");
        assertThat(result.judgeReason()).isEqualTo("判定模型未提供有效結果。");
    }

    @Test
    void evaluateTreatsMalformedJudgeResponseAsIncorrect() {
        AiRiddleConfigEntity config = new AiRiddleConfigEntity();
        config.setAnswerCriteria("高山茶");

        AiRiddleResult result = service.evaluate(
                config,
                "提示內容",
                List.of(),
                "應該是烏龍茶",
                "這題看起來有機會對",
                Map.of()
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.answerAttempt()).isTrue();
        assertThat(result.judgeReason()).isEqualTo("判定模型輸出格式無法解析。");
    }
}
