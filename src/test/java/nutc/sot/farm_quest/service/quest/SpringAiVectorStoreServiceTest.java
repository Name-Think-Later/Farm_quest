package nutc.sot.farm_quest.service.quest;

import java.util.List;
import java.util.UUID;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringAiVectorStoreServiceTest {

    private static final UUID GAME_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID QUEST_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final VectorStore vectorStore = mock(VectorStore.class);
    private final SpringAiVectorStoreService service = new SpringAiVectorStoreService(vectorStore);

    @Test
    void searchBuildsScopedRequest() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        service.search("茶葉蛋", context());

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(captor.capture());
        SearchRequest request = captor.getValue();

        assertThat(request.getQuery()).isEqualTo("茶葉蛋");
        assertThat(request.getTopK()).isEqualTo(4);
        assertThat(request.getFilterExpression()).isNotNull();

        Filter.Expression root = request.getFilterExpression();
        assertThat(root.type()).isEqualTo(Filter.ExpressionType.AND);

        Filter.Expression left = (Filter.Expression) root.left();
        Filter.Expression right = (Filter.Expression) root.right();

        assertEqualityExpression(left, "gameId", GAME_ID.toString());
        assertEqualityExpression(right, "questId", QUEST_ID.toString());
    }

    @Test
    void searchMapsVectorStoreErrorsToQuestException() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> service.search("茶葉蛋", context()))
                .isInstanceOf(QuestException.class)
                .hasMessage("RAG retrieval failed");
    }

    private void assertEqualityExpression(Filter.Expression expression, String key, String value) {
        assertThat(expression.type()).isEqualTo(Filter.ExpressionType.EQ);
        assertThat(((Filter.Key) expression.left()).key()).isEqualTo(key);
        assertThat(((Filter.Value) expression.right()).value()).isEqualTo(value);
    }

    private PromptPolicyService.PromptContext context() {
        return new PromptPolicyService.PromptContext(quest(), new QuestProgressEntity());
    }

    private QuestEntity quest() {
        GameEntity game = new GameEntity();
        game.setId(GAME_ID);

        QuestEntity quest = new QuestEntity();
        quest.setId(QUEST_ID);
        quest.setGame(game);
        return quest;
    }
}
