package nutc.sot.farm_quest.service.quest;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.quest.LocationVerificationRequest;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.LocationEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

    public LocationVerificationResult verify(LocationEntity location, LocationVerificationRequest request) {
        if (Boolean.TRUE.equals(request.permissionDenied())) {
            throw new QuestException(QuestErrorCode.GPS_PERMISSION_REQUIRED, HttpStatus.BAD_REQUEST, "GPS permission is required");
        }
        if (request.latitude() == null || request.longitude() == null || request.accuracyMeters() == null) {
            throw new QuestException(QuestErrorCode.LOCATION_REQUIRED, HttpStatus.BAD_REQUEST, "Location payload is required");
        }
        if (request.accuracyMeters() > location.getMaxAccuracyMeters()) {
            throw new QuestException(QuestErrorCode.LOCATION_ACCURACY_TOO_LOW, HttpStatus.BAD_REQUEST, "GPS accuracy is too low");
        }

        double distanceMeters = distanceMeters(
                request.latitude().doubleValue(),
                request.longitude().doubleValue(),
                location.getLatitude().doubleValue(),
                location.getLongitude().doubleValue()
        );
        if (distanceMeters > location.getRadiusMeters()) {
            throw new QuestException(QuestErrorCode.LOCATION_TOO_FAR, HttpStatus.BAD_REQUEST, "Current location is outside quest hotspot");
        }

        return new LocationVerificationResult(
                true,
                distanceMeters,
                request.accuracyMeters(),
                OffsetDateTime.now()
        );
    }

    private double distanceMeters(double latitude1, double longitude1, double latitude2, double longitude2) {
        double earthRadiusMeters = 6_371_000d;
        double latitudeDelta = Math.toRadians(latitude2 - latitude1);
        double longitudeDelta = Math.toRadians(longitude2 - longitude1);
        double latitudeRadians1 = Math.toRadians(latitude1);
        double latitudeRadians2 = Math.toRadians(latitude2);

        double haversine = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
                + Math.cos(latitudeRadians1) * Math.cos(latitudeRadians2)
                * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        double arc = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        return earthRadiusMeters * arc;
    }

    public record LocationVerificationResult(
            boolean passed,
            double distanceMeters,
            double accuracyMeters,
            OffsetDateTime verifiedAt
    ) {
    }
}
