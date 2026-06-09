package nutc.sot.farm_quest.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminAiRiddleConfigRequest(
        @NotBlank(message = "riddlePrompt is required")
        String riddlePrompt,
        @NotBlank(message = "answerCriteria is required")
        String answerCriteria,
        @NotBlank(message = "spoilerPolicy is required")
        String spoilerPolicy,
        @NotBlank(message = "completionPolicy is required")
        String completionPolicy,
        @NotBlank(message = "status is required")
        String status
) {
}
