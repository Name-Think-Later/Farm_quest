package nutc.sot.farm_quest.service.admin;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.admin.AdminAiRiddleConfigListResponse;
import nutc.sot.farm_quest.dto.admin.AdminAiRiddleConfigRequest;
import nutc.sot.farm_quest.dto.admin.AdminAiRiddleConfigResponse;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConfigEntity;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.repository.AiRiddleConfigRepository;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import nutc.sot.farm_quest.persistence.repository.QuestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminAiRiddleService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "INACTIVE");

    private final AiRiddleConfigRepository aiRiddleConfigRepository;
    private final QuestRepository questRepository;
    private final GameRepository gameRepository;
    private final AuthProperties authProperties;

    @Transactional(readOnly = true)
    public AdminAiRiddleConfigListResponse getAiRiddles() {
        GameEntity game = getCurrentGame();
        return new AdminAiRiddleConfigListResponse(aiRiddleConfigRepository.findByQuest_Game_IdOrderByQuest_SortOrderAsc(game.getId()).stream()
                .map(this::toResponse)
                .toList());
    }

    @Transactional
    public AdminAiRiddleConfigResponse upsertAiRiddleConfig(UUID questId, AdminAiRiddleConfigRequest request) {
        QuestEntity quest = getQuestInCurrentGame(questId);
        OffsetDateTime now = OffsetDateTime.now();
        AiRiddleConfigEntity config = aiRiddleConfigRepository.findByQuest_Id(questId)
                .orElseGet(() -> createConfig(quest, now));

        config.setRiddlePrompt(normalizeRequired(request.riddlePrompt(), "riddlePrompt"));
        config.setAnswerCriteria(normalizeRequired(request.answerCriteria(), "answerCriteria"));
        config.setSpoilerPolicy(normalizeRequired(request.spoilerPolicy(), "spoilerPolicy"));
        config.setCompletionPolicy(normalizeRequired(request.completionPolicy(), "completionPolicy"));
        config.setStatus(normalizeStatus(request.status()));
        config.setUpdatedAt(now);
        return toResponse(aiRiddleConfigRepository.save(config));
    }

    private AiRiddleConfigEntity createConfig(QuestEntity quest, OffsetDateTime now) {
        AiRiddleConfigEntity config = new AiRiddleConfigEntity();
        config.setId(UUID.randomUUID());
        config.setQuest(quest);
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        return config;
    }

    private QuestEntity getQuestInCurrentGame(UUID questId) {
        GameEntity game = getCurrentGame();
        return questRepository.findById(questId)
                .filter(quest -> quest.getGame().getId().equals(game.getId()))
                .orElseThrow(() -> new QuestException(QuestErrorCode.QUEST_NOT_FOUND, HttpStatus.NOT_FOUND, "Quest not found"));
    }

    private GameEntity getCurrentGame() {
        String gameCode = authProperties.getGameCode();
        if (!StringUtils.hasText(gameCode)) {
            throw new QuestException(QuestErrorCode.GAME_NOT_FOUND, HttpStatus.SERVICE_UNAVAILABLE, "Game code is not configured");
        }
        return gameRepository.findByCode(gameCode)
                .orElseThrow(() -> new QuestException(QuestErrorCode.GAME_NOT_FOUND, HttpStatus.NOT_FOUND, "Game not found"));
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = normalizeRequired(status, "status").toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, "AI riddle status is invalid");
        }
        return normalizedStatus;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private AdminAiRiddleConfigResponse toResponse(AiRiddleConfigEntity config) {
        return new AdminAiRiddleConfigResponse(
                config.getQuest().getId(),
                config.getQuest().getCode(),
                config.getQuest().getTitle(),
                config.getRiddlePrompt(),
                config.getAnswerCriteria(),
                config.getSpoilerPolicy(),
                config.getCompletionPolicy(),
                config.getStatus(),
                config.getUpdatedAt()
        );
    }
}
