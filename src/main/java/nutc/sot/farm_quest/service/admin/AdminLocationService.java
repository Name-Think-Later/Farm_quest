package nutc.sot.farm_quest.service.admin;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.admin.AdminLocationListResponse;
import nutc.sot.farm_quest.dto.admin.AdminLocationRequest;
import nutc.sot.farm_quest.dto.admin.AdminLocationResponse;
import nutc.sot.farm_quest.dto.admin.AdminLocationUpdateRequest;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.LocationEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import nutc.sot.farm_quest.persistence.repository.LocationRepository;
import nutc.sot.farm_quest.persistence.repository.QuestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminLocationService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "ACTIVE", "DISABLED");

    private final LocationRepository locationRepository;
    private final GameRepository gameRepository;
    private final QuestRepository questRepository;
    private final AuthProperties authProperties;

    @Transactional(readOnly = true)
    public AdminLocationListResponse getLocations() {
        GameEntity game = getCurrentGame();
        return new AdminLocationListResponse(locationRepository.findByGameIdOrderByQuestSortOrderAscLocationSortOrderAsc(game.getId()).stream()
                .map(this::toResponse)
                .toList());
    }

    @Transactional
    public AdminLocationResponse createLocation(AdminLocationRequest request) {
        GameEntity game = getCurrentGame();
        QuestEntity quest = getQuestInCurrentGame(request.questId(), game);
        OffsetDateTime now = OffsetDateTime.now();

        LocationEntity location = new LocationEntity();
        location.setId(UUID.randomUUID());
        location.setQuest(quest);
        location.setName(normalizeRequired(request.name(), "name"));
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setRadiusMeters(request.radiusMeters());
        location.setMaxAccuracyMeters(request.maxAccuracyMeters());
        location.setHintText(StringUtils.hasText(request.hintText()) ? request.hintText().trim() : null);
        location.setStatus(normalizeStatus(request.status()));
        location.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 1);
        location.setPrimary(request.primary() != null ? request.primary() : false);
        location.setCreatedAt(now);
        location.setUpdatedAt(now);

        return toResponse(locationRepository.save(location));
    }

    @Transactional
    public AdminLocationResponse updateLocation(UUID locationId, AdminLocationUpdateRequest request) {
        LocationEntity location = getLocationInCurrentGame(locationId);
        location.setName(normalizeRequired(request.name(), "name"));
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setRadiusMeters(request.radiusMeters());
        location.setMaxAccuracyMeters(request.maxAccuracyMeters());
        location.setHintText(StringUtils.hasText(request.hintText()) ? request.hintText().trim() : null);
        location.setStatus(normalizeStatus(request.status()));
        location.setSortOrder(request.sortOrder());
        location.setPrimary(request.primary());
        location.setUpdatedAt(OffsetDateTime.now());
        return toResponse(locationRepository.save(location));
    }

    private LocationEntity getLocationInCurrentGame(UUID locationId) {
        GameEntity game = getCurrentGame();
        return locationRepository.findById(locationId)
                .filter(location -> location.getQuest().getGame().getId().equals(game.getId()))
                .orElseThrow(() -> new QuestException(QuestErrorCode.LOCATION_NOT_FOUND, HttpStatus.NOT_FOUND, "Location not found"));
    }

    private QuestEntity getQuestInCurrentGame(UUID questId, GameEntity game) {
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

    private AdminLocationResponse toResponse(LocationEntity location) {
        return new AdminLocationResponse(
                location.getId(),
                location.getQuest().getId(),
                location.getQuest().getTitle(),
                location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getRadiusMeters(),
                location.getMaxAccuracyMeters(),
                location.getHintText(),
                location.getStatus(),
                location.getSortOrder(),
                Boolean.TRUE.equals(location.getPrimary()),
                location.getUpdatedAt()
        );
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "ACTIVE";
        }
        String normalizedStatus = status.toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, "Location status is invalid");
        }
        return normalizedStatus;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }
}
