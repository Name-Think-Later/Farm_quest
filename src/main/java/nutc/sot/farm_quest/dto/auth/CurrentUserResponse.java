package nutc.sot.farm_quest.dto.auth;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CurrentUserResponse(
        boolean authenticated,
        UUID visitorAccountId,
        String email,
        OffsetDateTime sessionExpiresAt
) {
}
