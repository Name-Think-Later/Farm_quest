package nutc.sot.farm_quest.service.game;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.game.GameEntryResponse;
import nutc.sot.farm_quest.dto.game.GameStateResponse;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.persistence.repository.AiRiddleConfigRepository;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import nutc.sot.farm_quest.persistence.repository.QuestRepository;
import nutc.sot.farm_quest.service.auth.SessionService;
import nutc.sot.farm_quest.service.quest.ProgressService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final QuestRepository questRepository;
    private final SessionService sessionService;
    private final ProgressService progressService;
    private final AiRiddleConfigRepository aiRiddleConfigRepository;

    @Transactional(readOnly = true)
    public GameEntryResponse getActiveGame() {
        GameEntity game = gameRepository.findByCode("farm-quest-mvp")
                .orElseThrow(() -> new QuestException(QuestErrorCode.GAME_NOT_FOUND, HttpStatus.NOT_FOUND, "Active game not found"));
        return new GameEntryResponse(
                game.getId(),
                game.getCode(),
                game.getName(),
                game.getEntryPath(),
                game.getStartsAt(),
                game.getEndsAt()
        );
    }

    @Transactional(readOnly = true)
    public GameStateResponse getGameState(String token) {
        VisitorSessionEntity session = sessionService.requireActiveSession(token);
        QuestProgressEntity currentProgress = progressService.findCurrentProgress(session.getVisitorAccount().getId(), session.getGame().getId())
                .orElseGet(() -> firstQuestProgressOrNull(session));

        String progressStatus = currentProgress == null ? "NOT_STARTED" : currentProgress.getStatus();
        boolean gpsVerified = isGpsVerified(progressStatus);
        boolean aiRiddleAvailable = isAiRiddleAvailable(currentProgress);
        return new GameStateResponse(
                session.getGame().getId(),
                session.getVisitorAccount().getId(),
                currentProgress == null ? null : currentProgress.getQuest().getId(),
                currentProgress == null ? null : currentProgress.getQuest().getTitle(),
                progressStatus,
                gpsVerified,
                aiRiddleAvailable,
                nextStep(progressStatus)
        );
    }

    private QuestProgressEntity firstQuestProgressOrNull(VisitorSessionEntity session) {
        QuestEntity firstQuest = questRepository.findByStatusOrderBySortOrderAsc("ACTIVE")
                .stream()
                .filter(quest -> quest.getGame().getId().equals(session.getGame().getId()))
                .findFirst()
                .orElse(null);
        if (firstQuest == null) {
            return null;
        }
        return progressService.findProgress(session.getVisitorAccount().getId(), firstQuest.getId()).orElse(null);
    }

    private boolean isGpsVerified(String progressStatus) {
        return List.of("LOCATION_VERIFIED", "AI_RIDDLE_STARTED", "COMPLETED").contains(progressStatus);
    }

    private boolean isAiRiddleAvailable(QuestProgressEntity progress) {
        if (progress == null) {
            return false;
        }
        if (!List.of("LOCATION_VERIFIED", "AI_RIDDLE_STARTED").contains(progress.getStatus())) {
            return false;
        }
        return aiRiddleConfigRepository.findByQuest_IdAndStatus(progress.getQuest().getId(), "ACTIVE").isPresent();
    }

    private String nextStep(String progressStatus) {
        return switch (progressStatus) {
            case "LOCATION_VERIFIED", "AI_RIDDLE_STARTED", "COMPLETED" -> "AI_RIDDLE_AVAILABLE";
            case "STARTED" -> "VERIFY_LOCATION";
            default -> "START_QUEST";
        };
    }
}
