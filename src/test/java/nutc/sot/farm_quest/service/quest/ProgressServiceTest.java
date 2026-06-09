package nutc.sot.farm_quest.service.quest;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConversationEntity;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.repository.QuestProgressRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProgressServiceTest {

    private final QuestProgressRepository questProgressRepository = mock(QuestProgressRepository.class);
    private final ProgressService progressService = new ProgressService(questProgressRepository);

    @Test
    void startQuestCreatesStartedProgressWhenMissing() {
        VisitorAccountEntity visitorAccount = visitorAccount();
        QuestEntity quest = quest(visitorAccount.getGame());

        when(questProgressRepository.findByVisitorAccount_IdAndQuest_Id(visitorAccount.getId(), quest.getId())).thenReturn(Optional.empty());
        when(questProgressRepository.save(any(QuestProgressEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuestProgressEntity progress = progressService.startQuest(visitorAccount, quest);

        assertThat(progress.getStatus()).isEqualTo("STARTED");
        assertThat(progress.getStartedAt()).isNotNull();
    }

    @Test
    void markLocationVerifiedIsIdempotentForRepeatedGpsSubmission() {
        QuestProgressEntity progress = existingProgress("LOCATION_VERIFIED");

        QuestProgressEntity result = progressService.markLocationVerified(progress, OffsetDateTime.now());

        assertThat(result.getStatus()).isEqualTo("LOCATION_VERIFIED");
        assertThat(result.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void markLocationVerifiedRejectsUnstartedQuest() {
        QuestProgressEntity progress = existingProgress("NOT_STARTED");

        assertThatThrownBy(() -> progressService.markLocationVerified(progress, OffsetDateTime.now()))
                .isInstanceOf(QuestException.class)
                .hasMessage("Quest must be started before GPS verification");
    }

    @Test
    void markAiRiddleStartedTransitionsProgress() {
        QuestProgressEntity progress = existingProgress("LOCATION_VERIFIED");
        AiRiddleConversationEntity conversation = conversation();
        when(questProgressRepository.save(any(QuestProgressEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuestProgressEntity result = progressService.markAiRiddleStarted(progress, conversation);

        assertThat(result.getStatus()).isEqualTo("AI_RIDDLE_STARTED");
        assertThat(result.getLastAiConversation()).isEqualTo(conversation);
    }

    @Test
    void markCompletedFromAiRiddleTransitionsProgress() {
        QuestProgressEntity progress = existingProgress("AI_RIDDLE_STARTED");
        AiRiddleConversationEntity conversation = conversation();
        when(questProgressRepository.save(any(QuestProgressEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuestProgressEntity result = progressService.markCompletedFromAiRiddle(progress, conversation, OffsetDateTime.now());

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getLastAiConversation()).isEqualTo(conversation);
    }

    private VisitorAccountEntity visitorAccount() {
        GameEntity game = new GameEntity();
        game.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        VisitorAccountEntity visitorAccount = new VisitorAccountEntity();
        visitorAccount.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        visitorAccount.setGame(game);
        return visitorAccount;
    }

    private QuestEntity quest(GameEntity game) {
        QuestEntity quest = new QuestEntity();
        quest.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        quest.setGame(game);
        quest.setStatus("ACTIVE");
        return quest;
    }

    private AiRiddleConversationEntity conversation() {
        AiRiddleConversationEntity conversation = new AiRiddleConversationEntity();
        conversation.setId(UUID.fromString("77777777-7777-7777-7777-777777777777"));
        return conversation;
    }

    private QuestProgressEntity existingProgress(String status) {
        QuestProgressEntity progress = new QuestProgressEntity();
        progress.setId(UUID.fromString("99999999-9999-9999-9999-999999999999"));
        progress.setStatus(status);
        progress.setAttemptCount(1);
        progress.setStartedAt(OffsetDateTime.now().minusMinutes(3));
        progress.setUpdatedAt(OffsetDateTime.now().minusMinutes(1));
        return progress;
    }
}
