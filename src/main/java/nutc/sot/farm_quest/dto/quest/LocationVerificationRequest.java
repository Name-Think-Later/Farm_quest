package nutc.sot.farm_quest.dto.quest;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record LocationVerificationRequest(
        @NotNull(message = "permissionDenied is required")
        Boolean permissionDenied,
        @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "latitude must be <= 90")
        BigDecimal latitude,
        @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "longitude must be <= 180")
        BigDecimal longitude,
        @PositiveOrZero(message = "accuracyMeters must be >= 0")
        Double accuracyMeters
) {
}
