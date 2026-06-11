package nutc.sot.farm_quest.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminCouponCampaignRequest(
        @NotNull(message = "questId is required")
        UUID questId,
        @NotBlank(message = "code is required")
        String code,
        @NotBlank(message = "title is required")
        String title,
        String description,
        String status,
        OffsetDateTime validFrom,
        OffsetDateTime validUntil
) {
}
