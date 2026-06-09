package nutc.sot.farm_quest.service.admin;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.admin.AdminQuestListResponse;
import nutc.sot.farm_quest.dto.admin.AdminQuestRequest;
import nutc.sot.farm_quest.dto.admin.AdminQuestResponse;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import nutc.sot.farm_quest.persistence.repository.QuestRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminQuestService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "ACTIVE", "DISABLED");

    private final QuestRepository questRepository;
    private final GameRepository gameRepository;
    private final AuthProperties authProperties;

    @Transactional(readOnly = true)
    public AdminQuestListResponse getQuests() {
        GameEntity game = getCurrentGame();
        return new AdminQuestListResponse(questRepository.findByGame_IdOrderBySortOrderAsc(game.getId()).stream()
                .map(this::toResponse)
                .toList());
    }

    @Transactional
    public AdminQuestResponse createQuest(AdminQuestRequest request) {
        GameEntity game = getCurrentGame();
        OffsetDateTime now = OffsetDateTime.now();

        QuestEntity quest = new QuestEntity();
        quest.setId(UUID.randomUUID());
        quest.setGame(game);
        quest.setCode(normalizeRequired(request.code(), "code"));
        quest.setTitle(normalizeRequired(request.title(), "title"));
        quest.setDescription(normalizeRequired(request.description(), "description"));
        quest.setSortOrder(request.sortOrder());
        quest.setStatus(normalizeStatus(request.status()));
        quest.setCreatedAt(now);
        quest.setUpdatedAt(now);

        return toResponse(saveQuest(quest));
    }

    @Transactional
    public AdminQuestResponse updateQuest(UUID questId, AdminQuestRequest request) {
        QuestEntity quest = getQuestInCurrentGame(questId);
        quest.setCode(normalizeRequired(request.code(), "code"));
        quest.setTitle(normalizeRequired(request.title(), "title"));
        quest.setDescription(normalizeRequired(request.description(), "description"));
        quest.setSortOrder(request.sortOrder());
        quest.setStatus(normalizeStatus(request.status()));
        quest.setUpdatedAt(OffsetDateTime.now());
        return toResponse(saveQuest(quest));
    }

    private QuestEntity saveQuest(QuestEntity quest) {
        try {
            return questRepository.save(quest);
        } catch (DataIntegrityViolationException exception) {
            throw new QuestException(QuestErrorCode.ADMIN_RESOURCE_CONFLICT, HttpStatus.CONFLICT, "Quest code or sort order already exists");
        }
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
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, "Quest status is invalid");
        }
        return normalizedStatus;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private AdminQuestResponse toResponse(QuestEntity quest) {
        return new AdminQuestResponse(
                quest.getId(),
                quest.getCode(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getSortOrder(),
                quest.getStatus(),
                quest.getUpdatedAt()
        );
    }
}
