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
    void evaluateMarksPlainCorrectAnswerAsCorrect() {
        AiRiddleConfigEntity config = new AiRiddleConfigEntity();
        config.setAnswerCriteria("高山茶");

        AiRiddleResult result = service.evaluate(
                config,
                "答對了",
                List.of(),
                "高山茶",
                Map.of()
        );

        assertThat(result.correct()).isTrue();
        assertThat(result.answerAttempt()).isTrue();
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
                Map.of()
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.answerAttempt()).isFalse();
    }
}
