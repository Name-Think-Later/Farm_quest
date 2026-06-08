package nutc.sot.farm_quest.service.quest;

import java.math.BigDecimal;
import nutc.sot.farm_quest.dto.quest.LocationVerificationRequest;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.LocationEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationServiceTest {

    private final LocationService locationService = new LocationService();

    @Test
    void verifyPassesWithinRadiusAndAccuracy() {
        LocationEntity location = location(24.147736, 120.673648, 30, 50);

        var result = locationService.verify(location, new LocationVerificationRequest(
                false,
                BigDecimal.valueOf(24.147736),
                BigDecimal.valueOf(120.673648),
                10.0
        ));

        assertThat(result.passed()).isTrue();
        assertThat(result.distanceMeters()).isLessThanOrEqualTo(1.0);
    }

    @Test
    void verifyFailsWhenPermissionDenied() {
        LocationEntity location = location(24.147736, 120.673648, 30, 50);

        assertThatThrownBy(() -> locationService.verify(location, new LocationVerificationRequest(
                true,
                null,
                null,
                null
        ))).isInstanceOf(QuestException.class)
                .hasMessage("GPS permission is required");
    }

    @Test
    void verifyFailsWhenAccuracyTooLow() {
        LocationEntity location = location(24.147736, 120.673648, 30, 50);

        assertThatThrownBy(() -> locationService.verify(location, new LocationVerificationRequest(
                false,
                BigDecimal.valueOf(24.147736),
                BigDecimal.valueOf(120.673648),
                55.0
        ))).isInstanceOf(QuestException.class)
                .hasMessage("GPS accuracy is too low");
    }

    @Test
    void verifyFailsWhenTooFar() {
        LocationEntity location = location(24.147736, 120.673648, 30, 50);

        assertThatThrownBy(() -> locationService.verify(location, new LocationVerificationRequest(
                false,
                BigDecimal.valueOf(24.150000),
                BigDecimal.valueOf(120.680000),
                10.0
        ))).isInstanceOf(QuestException.class)
                .hasMessage("Current location is outside quest hotspot");
    }

    private LocationEntity location(double latitude, double longitude, int radiusMeters, int maxAccuracyMeters) {
        LocationEntity location = new LocationEntity();
        location.setLatitude(BigDecimal.valueOf(latitude));
        location.setLongitude(BigDecimal.valueOf(longitude));
        location.setRadiusMeters(radiusMeters);
        location.setMaxAccuracyMeters(maxAccuracyMeters);
        return location;
    }
}
