package nutc.sot.farm_quest.dto.system;

import java.time.OffsetDateTime;

public record HealthResponse(
        String status,
        String application,
        OffsetDateTime timestamp,
        boolean seedDataReady
) {
}
