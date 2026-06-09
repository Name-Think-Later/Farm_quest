package nutc.sot.farm_quest.service.game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.dto.game.GameStateResponse;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.persistence.repository.AiRiddleConfigRepository;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import nutc.sot.farm_quest.persistence.repository.QuestRepository;
import nutc.sot.farm_quest.service.auth.SessionService;
import nutc.sot.farm_quest.service.quest.ProgressService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GameServiceTest {

    private static final UUID GAME_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACCOUNT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID QUEST_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final GameRepository gameRepository = mock(GameRepository.class);
    private final QuestRepository questRepository = mock(QuestRepository.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final ProgressService progressService = mock(ProgressService.class);
    private final AiRiddleConfigRepository aiRiddleConfigRepository = mock(AiRiddleConfigRepository.class);
    private final GameService gameService = new GameService(
            gameRepository,
            questRepository,
            sessionService,
            progressService,
            aiRiddleConfigRepository
    );

    @Test
    void getGameStateReturnsFalseWhenQuestIsOnlyStarted() {
        QuestProgressEntity progress = progress("STARTED");
        when(sessionService.requireActiveSession("session-token")).thenReturn(session());
        when(progressService.findCurrentProgress(ACCOUNT_ID, GAME_ID)).thenReturn(Optional.of(progress));

        GameStateResponse response = gameService.getGameState("session-token");

        assertThat(response.progressStatus()).isEqualTo("STARTED");
        assertThat(response.gpsVerified()).isFalse();
        assertThat(response.aiRiddleAvailable()).isFalse();
        verifyNoInteractions(aiRiddleConfigRepository);
    }

    @Test
    void getGameStateReturnsTrueWhenLocationVerifiedAndActiveConfigExists() {
        QuestProgressEntity progress = progress("LOCATION_VERIFIED");
        when(sessionService.requireActiveSession("session-token")).thenReturn(session());
        when(progressService.findCurrentProgress(ACCOUNT_ID, GAME_ID)).thenReturn(Optional.of(progress));
        when(aiRiddleConfigRepository.findByQuest_IdAndStatus(QUEST_ID, "ACTIVE")).thenReturn(Optional.of(mock()));

        GameStateResponse response = gameService.getGameState("session-token");

        assertThat(response.gpsVerified()).isTrue();
        assertThat(response.aiRiddleAvailable()).isTrue();
    }

    @Test
    void getGameStateReturnsFalseWhenLocationVerifiedButActiveConfigMissing() {
        QuestProgressEntity progress = progress("LOCATION_VERIFIED");
        when(sessionService.requireActiveSession("session-token")).thenReturn(session());
        when(progressService.findCurrentProgress(ACCOUNT_ID, GAME_ID)).thenReturn(Optional.of(progress));
        when(aiRiddleConfigRepository.findByQuest_IdAndStatus(QUEST_ID, "ACTIVE")).thenReturn(Optional.empty());

        GameStateResponse response = gameService.getGameState("session-token");

        assertThat(response.gpsVerified()).isTrue();
        assertThat(response.aiRiddleAvailable()).isFalse();
    }

    @Test
    void getGameStateReturnsTrueWhenAiRiddleAlreadyStartedAndActiveConfigExists() {
        QuestProgressEntity progress = progress("AI_RIDDLE_STARTED");
        when(sessionService.requireActiveSession("session-token")).thenReturn(session());
        when(progressService.findCurrentProgress(ACCOUNT_ID, GAME_ID)).thenReturn(Optional.of(progress));
        when(aiRiddleConfigRepository.findByQuest_IdAndStatus(QUEST_ID, "ACTIVE")).thenReturn(Optional.of(mock()));

        GameStateResponse response = gameService.getGameState("session-token");

        assertThat(response.gpsVerified()).isTrue();
        assertThat(response.aiRiddleAvailable()).isTrue();
    }

    @Test
    void getGameStateReturnsFalseWhenQuestAlreadyCompleted() {
        QuestProgressEntity progress = progress("COMPLETED");
        when(sessionService.requireActiveSession("session-token")).thenReturn(session());
        when(progressService.findCurrentProgress(ACCOUNT_ID, GAME_ID)).thenReturn(Optional.of(progress));

        GameStateResponse response = gameService.getGameState("session-token");

        assertThat(response.gpsVerified()).isTrue();
        assertThat(response.aiRiddleAvailable()).isFalse();
        verifyNoInteractions(aiRiddleConfigRepository);
    }

    @Test
    void getGameStateFallsBackToFirstQuestWhenCurrentProgressMissing() {
        QuestEntity firstQuest = quest();
        when(sessionService.requireActiveSession("session-token")).thenReturn(session());
        when(progressService.findCurrentProgress(ACCOUNT_ID, GAME_ID)).thenReturn(Optional.empty());
        when(questRepository.findByStatusOrderBySortOrderAsc("ACTIVE")).thenReturn(List.of(firstQuest));
        when(progressService.findProgress(ACCOUNT_ID, QUEST_ID)).thenReturn(Optional.empty());

        GameStateResponse response = gameService.getGameState("session-token");

        assertThat(response.progressStatus()).isEqualTo("NOT_STARTED");
        assertThat(response.currentQuestId()).isNull();
        assertThat(response.aiRiddleAvailable()).isFalse();
        verify(questRepository).findByStatusOrderBySortOrderAsc("ACTIVE");
    }

    private VisitorSessionEntity session() {
        GameEntity game = game();
        VisitorAccountEntity account = new VisitorAccountEntity();
        account.setId(ACCOUNT_ID);
        account.setGame(game);

        VisitorSessionEntity session = new VisitorSessionEntity();
        session.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        session.setGame(game);
        session.setVisitorAccount(account);
        return session;
    }

    private QuestProgressEntity progress(String status) {
        QuestProgressEntity progress = new QuestProgressEntity();
        progress.setId(UUID.fromString("99999999-9999-9999-9999-999999999999"));
        progress.setQuest(quest());
        progress.setStatus(status);
        return progress;
    }

    private QuestEntity quest() {
        QuestEntity quest = new QuestEntity();
        quest.setId(QUEST_ID);
        quest.setGame(game());
        quest.setTitle("茶園謎題");
        quest.setStatus("ACTIVE");
        quest.setSortOrder(1);
        return quest;
    }

    private GameEntity game() {
        GameEntity game = new GameEntity();
        game.setId(GAME_ID);
        return game;
    }
}
