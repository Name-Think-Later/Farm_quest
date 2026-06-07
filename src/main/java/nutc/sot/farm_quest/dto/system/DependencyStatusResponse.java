package nutc.sot.farm_quest.dto.system;

import java.time.OffsetDateTime;
import java.util.List;

public record DependencyStatusResponse(
        String status,
        OffsetDateTime timestamp,
        List<DependencyItemResponse> dependencies
) {
}
