package nutc.sot.farm_quest.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminQuestResponse(
        UUID questId,
        String code,
        String title,
        String description,
        Integer sortOrder,
        String status,
        OffsetDateTime updatedAt
) {
}
