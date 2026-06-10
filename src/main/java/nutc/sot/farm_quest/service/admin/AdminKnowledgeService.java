package nutc.sot.farm_quest.service.admin;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.admin.KnowledgeDocumentListResponse;
import nutc.sot.farm_quest.dto.admin.KnowledgeDocumentRequest;
import nutc.sot.farm_quest.dto.admin.KnowledgeDocumentResponse;
import nutc.sot.farm_quest.dto.admin.ReindexKnowledgeRequest;
import nutc.sot.farm_quest.dto.admin.ReindexKnowledgeResponse;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import nutc.sot.farm_quest.persistence.entity.LocationEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import nutc.sot.farm_quest.persistence.repository.KnowledgeDocumentRepository;
import nutc.sot.farm_quest.persistence.repository.LocationRepository;
import nutc.sot.farm_quest.persistence.repository.QuestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminKnowledgeService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_INDEXED = "INDEXED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_REINDEX_QUEUED = "REINDEX_QUEUED";

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final QuestRepository questRepository;
    private final LocationRepository locationRepository;
    private final GameRepository gameRepository;
    private final AdminKnowledgeReindexService adminKnowledgeReindexService;
    private final AuthProperties authProperties;

    @Transactional(readOnly = true)
    public KnowledgeDocumentListResponse getKnowledgeDocuments() {
        GameEntity game = getCurrentGame();
        return new KnowledgeDocumentListResponse(knowledgeDocumentRepository.findByGame_IdOrderByUpdatedAtDesc(game.getId()).stream()
                .map(this::toResponse)
                .toList());
    }

    @Transactional
    public KnowledgeDocumentResponse createKnowledgeDocument(KnowledgeDocumentRequest request) {
        GameEntity game = getCurrentGame();
        QuestEntity quest = resolveQuest(request.questId(), game.getId());
        LocationEntity location = resolveLocation(request.locationId(), game.getId());

        if (location != null && quest == null) {
            quest = location.getQuest();
        }
        if (location != null && quest != null && !location.getQuest().getId().equals(quest.getId())) {
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, "Location does not belong to quest");
        }

        OffsetDateTime now = OffsetDateTime.now();
        KnowledgeDocumentEntity document = new KnowledgeDocumentEntity();
        document.setId(UUID.randomUUID());
        document.setGame(game);
        document.setQuest(quest);
        document.setLocation(location);
        document.setTitle(normalizeRequired(request.title(), "title"));
        document.setContent(normalizeRequired(request.content(), "content"));
        document.setSource(normalizeRequired(request.source(), "source"));
        document.setSpoilerLevel(normalizeRequired(request.spoilerLevel(), "spoilerLevel"));
        document.setVersion(nextVersion(quest, location));
        document.setEmbeddingStatus(STATUS_PENDING);
        document.setIndexedAt(null);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        KnowledgeDocumentEntity savedDocument = knowledgeDocumentRepository.save(document);
        adminKnowledgeReindexService.triggerReindexAsync(List.of(savedDocument.getId()));
        return toResponse(savedDocument);
    }

    @Transactional
    public ReindexKnowledgeResponse reindexKnowledge(ReindexKnowledgeRequest request) {
        GameEntity game = getCurrentGame();
        boolean fullRebuild = request != null && request.fullRebuild();
        List<KnowledgeDocumentEntity> documents = fullRebuild
                ? knowledgeDocumentRepository.findByGame_IdOrderByUpdatedAtDesc(game.getId())
                : queueTargetedReindexDocuments(game.getId());
        if (documents.isEmpty()) {
            return new ReindexKnowledgeResponse(true, 0, STATUS_REINDEX_QUEUED);
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (KnowledgeDocumentEntity document : documents) {
            document.setEmbeddingStatus(STATUS_PENDING);
            document.setIndexedAt(null);
            document.setUpdatedAt(now);
        }
        knowledgeDocumentRepository.saveAll(documents);
        adminKnowledgeReindexService.triggerReindexAsync(documents.stream().map(KnowledgeDocumentEntity::getId).toList());
        return new ReindexKnowledgeResponse(true, documents.size(), STATUS_REINDEX_QUEUED);
    }

    private List<KnowledgeDocumentEntity> queueTargetedReindexDocuments(UUID gameId) {
        List<KnowledgeDocumentEntity> pendingDocuments = knowledgeDocumentRepository.findByGame_IdAndEmbeddingStatusOrderByUpdatedAtDesc(gameId, STATUS_PENDING);
        if (!pendingDocuments.isEmpty()) {
            return pendingDocuments;
        }
        return knowledgeDocumentRepository.findByGame_IdAndEmbeddingStatusOrderByUpdatedAtDesc(gameId, STATUS_FAILED);
    }

    private int nextVersion(QuestEntity quest, LocationEntity location) {
        if (location != null) {
            return knowledgeDocumentRepository.findByLocation_IdOrderByVersionDescUpdatedAtDesc(location.getId()).stream()
                    .findFirst()
                    .map(document -> document.getVersion() + 1)
                    .orElse(1);
        }
        if (quest != null) {
            return knowledgeDocumentRepository.findByQuest_IdOrderByVersionDescUpdatedAtDesc(quest.getId()).stream()
                    .findFirst()
                    .map(document -> document.getVersion() + 1)
                    .orElse(1);
        }
        return 1;
    }

    private QuestEntity resolveQuest(UUID questId, UUID gameId) {
        if (questId == null) {
            return null;
        }
        return questRepository.findById(questId)
                .filter(quest -> quest.getGame().getId().equals(gameId))
                .orElseThrow(() -> new QuestException(QuestErrorCode.QUEST_NOT_FOUND, HttpStatus.NOT_FOUND, "Quest not found"));
    }

    private LocationEntity resolveLocation(UUID locationId, UUID gameId) {
        if (locationId == null) {
            return null;
        }
        return locationRepository.findById(locationId)
                .filter(location -> location.getQuest().getGame().getId().equals(gameId))
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

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new QuestException(QuestErrorCode.ADMIN_INVALID_REQUEST, HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private KnowledgeDocumentResponse toResponse(KnowledgeDocumentEntity document) {
        return new KnowledgeDocumentResponse(
                document.getId(),
                document.getQuest() == null ? null : document.getQuest().getId(),
                document.getLocation() == null ? null : document.getLocation().getId(),
                document.getTitle(),
                document.getSource(),
                document.getSpoilerLevel(),
                document.getVersion(),
                document.getEmbeddingStatus(),
                document.getIndexedAt(),
                document.getUpdatedAt()
        );
    }
}
