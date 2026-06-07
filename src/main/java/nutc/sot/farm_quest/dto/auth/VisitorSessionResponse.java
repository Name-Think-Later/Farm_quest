package nutc.sot.farm_quest.dto.auth;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VisitorSessionResponse(
        UUID visitorAccountId,
        String email,
        String sessionToken,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        boolean authenticated
) {
}
