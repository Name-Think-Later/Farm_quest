package nutc.sot.farm_quest.dto.quest;

import java.util.UUID;

public record LocationHintResponse(
        UUID questId,
        UUID locationId,
        String locationName,
        String hintText,
        int radiusMeters,
        int maxAccuracyMeters
) {
}
