package nutc.sot.farm_quest.service.admin;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.admin.AdminLocationHotspotRequest;
import nutc.sot.farm_quest.dto.admin.AdminLocationListResponse;
import nutc.sot.farm_quest.dto.admin.AdminLocationResponse;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.LocationEntity;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import nutc.sot.farm_quest.persistence.repository.LocationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminLocationService {

    private final LocationRepository locationRepository;
    private final GameRepository gameRepository;
    private final AuthProperties authProperties;

    @Transactional(readOnly = true)
    public AdminLocationListResponse getLocations() {
        GameEntity game = getCurrentGame();
        return new AdminLocationListResponse(locationRepository.findByGameIdOrderByQuestSortOrderAscLocationSortOrderAsc(game.getId()).stream()
                .map(this::toResponse)
                .toList());
    }

    @Transactional
    public AdminLocationResponse updateHotspot(java.util.UUID locationId, AdminLocationHotspotRequest request) {
        LocationEntity location = getLocationInCurrentGame(locationId);
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setRadiusMeters(request.radiusMeters());
        location.setMaxAccuracyMeters(request.maxAccuracyMeters());
        location.setHintText(StringUtils.hasText(request.hintText()) ? request.hintText().trim() : null);
        location.setUpdatedAt(OffsetDateTime.now());
        return toResponse(locationRepository.save(location));
    }

    private LocationEntity getLocationInCurrentGame(java.util.UUID locationId) {
        GameEntity game = getCurrentGame();
        return locationRepository.findById(locationId)
                .filter(location -> location.getQuest().getGame().getId().equals(game.getId()))
                .orElseThrow(() -> new QuestException(QuestErrorCode.LOCATION_NOT_FOUND, HttpStatus.NOT_FOUND, "Location not found"));
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
}
