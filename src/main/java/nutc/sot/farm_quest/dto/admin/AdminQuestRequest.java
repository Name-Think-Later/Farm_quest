package nutc.sot.farm_quest.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminQuestRequest(
        @NotBlank(message = "code is required")
        String code,
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "description is required")
        String description,
        @NotNull(message = "sortOrder is required")
        Integer sortOrder,
        @NotBlank(message = "status is required")
        String status
) {
}
