package nutc.sot.farm_quest.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record KnowledgeDocumentResponse(
        UUID documentId,
        UUID questId,
        UUID locationId,
        String title,
        String source,
        String spoilerLevel,
        Integer version,
        String embeddingStatus,
        OffsetDateTime indexedAt,
        OffsetDateTime updatedAt
) {
}
