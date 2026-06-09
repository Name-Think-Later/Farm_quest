package nutc.sot.farm_quest.service.quest;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.repository.QuestProgressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final QuestProgressRepository questProgressRepository;

    @Transactional(readOnly = true)
    public Optional<QuestProgressEntity> findProgress(UUID visitorAccountId, UUID questId) {
        return questProgressRepository.findByVisitorAccount_IdAndQuest_Id(visitorAccountId, questId);
    }

    @Transactional(readOnly = true)
    public Optional<QuestProgressEntity> findCurrentProgress(UUID visitorAccountId, UUID gameId) {
        return questProgressRepository.findFirstByVisitorAccount_IdAndGame_IdAndStatusOrderByUpdatedAtDesc(visitorAccountId, gameId, "STARTED")
                .or(() -> questProgressRepository.findFirstByVisitorAccount_IdAndGame_IdAndStatusOrderByUpdatedAtDesc(visitorAccountId, gameId, "LOCATION_VERIFIED"))
                .or(() -> questProgressRepository.findFirstByVisitorAccount_IdAndGame_IdAndStatusOrderByUpdatedAtDesc(visitorAccountId, gameId, "AI_RIDDLE_STARTED"));
    }

    @Transactional
    public QuestProgressEntity startQuest(VisitorAccountEntity visitorAccount, QuestEntity quest) {
        QuestProgressEntity progress = findProgress(visitorAccount.getId(), quest.getId())
                .orElseGet(() -> createProgress(visitorAccount, quest));
        if ("LOCATION_VERIFIED".equals(progress.getStatus()) || "AI_RIDDLE_STARTED".equals(progress.getStatus()) || "COMPLETED".equals(progress.getStatus())) {
            return progress;
        }
        if (!"STARTED".equals(progress.getStatus())) {
            OffsetDateTime now = OffsetDateTime.now();
            progress.setStatus("STARTED");
            progress.setStartedAt(progress.getStartedAt() == null ? now : progress.getStartedAt());
            progress.setUpdatedAt(now);
            progress = questProgressRepository.save(progress);
        }
        return progress;
    }

    @Transactional
    public QuestProgressEntity markLocationVerified(QuestProgressEntity progress, OffsetDateTime verifiedAt) {
        if (!"STARTED".equals(progress.getStatus()) && !"LOCATION_VERIFIED".equals(progress.getStatus())) {
            throw new QuestException(QuestErrorCode.QUEST_NOT_STARTED, HttpStatus.BAD_REQUEST, "Quest must be started before GPS verification");
        }
        if (!"LOCATION_VERIFIED".equals(progress.getStatus())) {
            progress.setStatus("LOCATION_VERIFIED");
            progress.setLocationVerifiedAt(verifiedAt);
            progress.setAttemptCount(progress.getAttemptCount() == null ? 1 : progress.getAttemptCount() + 1);
            progress.setUpdatedAt(verifiedAt);
            progress = questProgressRepository.save(progress);
        }
        return progress;
    }

    @Transactional
    public QuestProgressEntity markAiRiddleStarted(QuestProgressEntity progress, nutc.sot.farm_quest.persistence.entity.AiRiddleConversationEntity conversation) {
        if (!"LOCATION_VERIFIED".equals(progress.getStatus()) && !"AI_RIDDLE_STARTED".equals(progress.getStatus())) {
            throw new QuestException(QuestErrorCode.QUEST_LOCATION_NOT_VERIFIED, HttpStatus.BAD_REQUEST, "Quest location is not verified");
        }
        progress.setLastAiConversation(conversation);
        if (!"AI_RIDDLE_STARTED".equals(progress.getStatus())) {
            progress.setStatus("AI_RIDDLE_STARTED");
        }
        progress.setUpdatedAt(OffsetDateTime.now());
        return questProgressRepository.save(progress);
    }

    @Transactional
    public QuestProgressEntity markCompletedFromAiRiddle(QuestProgressEntity progress,
                                                         nutc.sot.farm_quest.persistence.entity.AiRiddleConversationEntity conversation,
                                                         OffsetDateTime completedAt) {
        if ("COMPLETED".equals(progress.getStatus())) {
            return progress;
        }
        if (!"LOCATION_VERIFIED".equals(progress.getStatus()) && !"AI_RIDDLE_STARTED".equals(progress.getStatus())) {
            throw new QuestException(QuestErrorCode.QUEST_LOCATION_NOT_VERIFIED, HttpStatus.BAD_REQUEST, "Quest location is not verified");
        }
        progress.setStatus("COMPLETED");
        progress.setCompletedAt(completedAt);
        progress.setLastAiConversation(conversation);
        progress.setUpdatedAt(completedAt);
        return questProgressRepository.save(progress);
    }

    private QuestProgressEntity createProgress(VisitorAccountEntity visitorAccount, QuestEntity quest) {
        OffsetDateTime now = OffsetDateTime.now();
        QuestProgressEntity progress = new QuestProgressEntity();
        progress.setId(UUID.randomUUID());
        progress.setGame(quest.getGame());
        progress.setVisitorAccount(visitorAccount);
        progress.setQuest(quest);
        progress.setStatus("NOT_STARTED");
        progress.setLastHintLevel(0);
        progress.setAttemptCount(0);
        progress.setCreatedAt(now);
        progress.setUpdatedAt(now);
        return questProgressRepository.save(progress);
    }
}
