package nutc.sot.farm_quest.dto.admin;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record AdminLocationRequest(
        @NotNull(message = "questId is required")
        UUID questId,
        @NotNull(message = "name is required")
        @Size(min = 1, max = 100, message = "name must be between 1 and 100 characters")
        String name,
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
        String hintText,
        @Size(min = 1, max = 32, message = "status must be between 1 and 32 characters")
        String status,
        Integer sortOrder,
        Boolean primary
) {
}