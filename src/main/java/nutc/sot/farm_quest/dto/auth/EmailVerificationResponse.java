package nutc.sot.farm_quest.dto.auth;

import java.time.OffsetDateTime;

public record EmailVerificationResponse(
        String email,
        OffsetDateTime expiresAt,
        OffsetDateTime resendAvailableAt,
        String status
) {
}
