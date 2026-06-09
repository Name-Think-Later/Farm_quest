package nutc.sot.farm_quest.dto.quest;

import jakarta.validation.constraints.NotBlank;

public record AiRiddleMessageRequest(
        @NotBlank(message = "content is required")
        String content
) {
}
