package nutc.sot.farm_quest.dto.game;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GameEntryResponse(
        UUID gameId,
        String code,
        String name,
        String entryPath,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt
) {
}
