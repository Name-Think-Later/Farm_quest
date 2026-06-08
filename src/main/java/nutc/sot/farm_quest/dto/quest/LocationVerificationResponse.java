package nutc.sot.farm_quest.dto.quest;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LocationVerificationResponse(
        UUID questId,
        String status,
        boolean passed,
        double distanceMeters,
        double accuracyMeters,
        OffsetDateTime locationVerifiedAt,
        String nextStep
) {
}
