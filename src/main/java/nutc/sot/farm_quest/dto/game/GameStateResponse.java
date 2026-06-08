package nutc.sot.farm_quest.dto.game;

import java.util.UUID;

public record GameStateResponse(
        UUID gameId,
        UUID visitorAccountId,
        UUID currentQuestId,
        String currentQuestTitle,
        String progressStatus,
        boolean gpsVerified,
        boolean aiRiddleAvailable,
        String nextStep
) {
}
