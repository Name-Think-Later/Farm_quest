package nutc.sot.farm_quest.service.quest;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.quest.LocationHintResponse;
import nutc.sot.farm_quest.dto.quest.LocationVerificationRequest;
import nutc.sot.farm_quest.dto.quest.LocationVerificationResponse;
import nutc.sot.farm_quest.dto.quest.QuestDetailResponse;
import nutc.sot.farm_quest.dto.quest.QuestListResponse;
import nutc.sot.farm_quest.dto.quest.StartQuestResponse;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.LocationEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.persistence.repository.LocationRepository;
import nutc.sot.farm_quest.persistence.repository.QuestRepository;
import nutc.sot.farm_quest.service.auth.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final SessionService sessionService;
    private final QuestRepository questRepository;
    private final LocationRepository locationRepository;
    private final ProgressService progressService;
    private final LocationService locationService;

    @Transactional(readOnly = true)
    public QuestListResponse getVisibleQuests(String token) {
        VisitorSessionEntity session = sessionService.requireActiveSession(token);
        List<QuestDetailResponse> quests = questRepository.findByStatusOrderBySortOrderAsc("ACTIVE")
                .stream()
                .filter(quest -> quest.getGame().getId().equals(session.getGame().getId()))
                .map(quest -> toQuestDetail(session, quest))
                .toList();
        return new QuestListResponse(quests);
    }

    @Transactional(readOnly = true)
    public QuestDetailResponse getCurrentQuest(String token) {
        VisitorSessionEntity session = sessionService.requireActiveSession(token);
        QuestEntity currentQuest = questRepository.findByStatusOrderBySortOrderAsc("ACTIVE")
                .stream()
                .filter(quest -> quest.getGame().getId().equals(session.getGame().getId()))
                .filter(quest -> progressService.findProgress(session.getVisitorAccount().getId(), quest.getId())
                        .map(progress -> !"COMPLETED".equals(progress.getStatus()))
                        .orElse(true))
                .min(Comparator.comparing(QuestEntity::getSortOrder))
                .orElseThrow(() -> new QuestException(QuestErrorCode.QUEST_NOT_FOUND, HttpStatus.NOT_FOUND, "No current quest found"));
        return toQuestDetail(session, currentQuest);
    }

    @Transactional
    public StartQuestResponse startQuest(String token, UUID questId) {
        VisitorSessionEntity session = sessionService.requireActiveSession(token);
        QuestEntity quest = requireAvailableQuest(session, questId);
        QuestProgressEntity progress = progressService.startQuest(session.getVisitorAccount(), quest);
        return new StartQuestResponse(progress.getQuest().getId(), progress.getStatus(), progress.getStartedAt(), nextStep(progress.getStatus()));
    }

    @Transactional(readOnly = true)
    public LocationHintResponse getLocationHint(String token, UUID questId) {
        VisitorSessionEntity session = sessionService.requireActiveSession(token);
        QuestEntity quest = requireAvailableQuest(session, questId);
        LocationEntity location = requirePrimaryLocation(quest.getId());
        return new LocationHintResponse(
                quest.getId(),
                location.getId(),
                location.getName(),
                location.getHintText(),
                location.getRadiusMeters(),
                location.getMaxAccuracyMeters()
        );
    }

    @Transactional
    public LocationVerificationResponse verifyLocation(String token, UUID questId, LocationVerificationRequest request) {
        VisitorSessionEntity session = sessionService.requireActiveSession(token);
        QuestEntity quest = requireAvailableQuest(session, questId);
        QuestProgressEntity progress = progressService.findProgress(session.getVisitorAccount().getId(), quest.getId())
                .orElseThrow(() -> new QuestException(QuestErrorCode.QUEST_NOT_STARTED, HttpStatus.BAD_REQUEST, "Quest has not been started"));
        LocationEntity location = requirePrimaryLocation(quest.getId());
        LocationService.LocationVerificationResult verificationResult = locationService.verify(location, request);
        QuestProgressEntity updatedProgress = progressService.markLocationVerified(progress, verificationResult.verifiedAt());
        return new LocationVerificationResponse(
                quest.getId(),
                updatedProgress.getStatus(),
                verificationResult.passed(),
                verificationResult.distanceMeters(),
                verificationResult.accuracyMeters(),
                updatedProgress.getLocationVerifiedAt(),
                nextStep(updatedProgress.getStatus())
        );
    }

    private QuestEntity requireAvailableQuest(VisitorSessionEntity session, UUID questId) {
        QuestEntity quest = questRepository.findById(questId)
                .filter(item -> item.getGame().getId().equals(session.getGame().getId()))
                .orElseThrow(() -> new QuestException(QuestErrorCode.QUEST_NOT_FOUND, HttpStatus.NOT_FOUND, "Quest not found"));
        if (!"ACTIVE".equals(quest.getStatus())) {
            throw new QuestException(QuestErrorCode.QUEST_NOT_AVAILABLE, HttpStatus.BAD_REQUEST, "Quest is not available");
        }
        return quest;
    }

    private LocationEntity requirePrimaryLocation(UUID questId) {
        return locationRepository.findByPrimaryTrue()
                .stream()
                .filter(location -> location.getQuest().getId().equals(questId))
                .filter(location -> "ACTIVE".equals(location.getStatus()))
                .findFirst()
                .orElseThrow(() -> new QuestException(QuestErrorCode.LOCATION_REQUIRED, HttpStatus.NOT_FOUND, "Quest location is required"));
    }

    private QuestDetailResponse toQuestDetail(VisitorSessionEntity session, QuestEntity quest) {
        QuestProgressEntity progress = progressService.findProgress(session.getVisitorAccount().getId(), quest.getId()).orElse(null);
        String progressStatus = progress == null ? "NOT_STARTED" : progress.getStatus();
        return new QuestDetailResponse(
                quest.getId(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getSortOrder(),
                progressStatus,
                progress == null ? null : progress.getStartedAt(),
                progress == null ? null : progress.getLocationVerifiedAt(),
                isCurrentQuest(session, quest),
                nextStep(progressStatus)
        );
    }

    private boolean isCurrentQuest(VisitorSessionEntity session, QuestEntity quest) {
        return questRepository.findByStatusOrderBySortOrderAsc("ACTIVE")
                .stream()
                .filter(item -> item.getGame().getId().equals(session.getGame().getId()))
                .filter(item -> progressService.findProgress(session.getVisitorAccount().getId(), item.getId())
                        .map(progress -> !"COMPLETED".equals(progress.getStatus()))
                        .orElse(true))
                .findFirst()
                .map(currentQuest -> currentQuest.getId().equals(quest.getId()))
                .orElse(false);
    }

    private String nextStep(String progressStatus) {
        return switch (progressStatus) {
            case "LOCATION_VERIFIED", "AI_RIDDLE_STARTED", "COMPLETED" -> "AI_RIDDLE_AVAILABLE";
            case "STARTED" -> "VERIFY_LOCATION";
            default -> "START_QUEST";
        };
    }
}
