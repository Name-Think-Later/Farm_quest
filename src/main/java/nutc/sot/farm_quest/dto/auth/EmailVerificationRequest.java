package nutc.sot.farm_quest.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        String email
) {
}
