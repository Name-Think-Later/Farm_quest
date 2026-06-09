package nutc.sot.farm_quest.dto.admin;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminLocationResponse(
        UUID locationId,
        UUID questId,
        String questTitle,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        Integer maxAccuracyMeters,
        String hintText,
        String status,
        Integer sortOrder,
        boolean primary,
        OffsetDateTime updatedAt
) {
}
