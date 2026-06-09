package nutc.sot.farm_quest.dto.admin;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AdminLocationHotspotRequest(
        @NotNull(message = "latitude is required")
        @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "latitude must be <= 90")
        BigDecimal latitude,
        @NotNull(message = "longitude is required")
        @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "longitude must be <= 180")
        BigDecimal longitude,
        @NotNull(message = "radiusMeters is required")
        @Positive(message = "radiusMeters must be > 0")
        Integer radiusMeters,
        @NotNull(message = "maxAccuracyMeters is required")
        @Positive(message = "maxAccuracyMeters must be > 0")
        Integer maxAccuracyMeters,
        String hintText
) {
}
