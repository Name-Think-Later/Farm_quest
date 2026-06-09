package nutc.sot.farm_quest.dto.admin;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record KnowledgeDocumentRequest(
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "content is required")
        String content,
        @NotBlank(message = "source is required")
        String source,
        UUID questId,
        UUID locationId,
        @NotBlank(message = "spoilerLevel is required")
        String spoilerLevel
) {
}
