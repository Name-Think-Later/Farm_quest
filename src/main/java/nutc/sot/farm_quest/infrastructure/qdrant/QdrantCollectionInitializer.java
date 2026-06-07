package nutc.sot.farm_quest.infrastructure.qdrant;

import nutc.sot.farm_quest.config.QdrantProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "farm-quest.qdrant", name = "enabled", havingValue = "true")
public class QdrantCollectionInitializer implements ApplicationRunner {

    private final RestClient restClient;
    private final QdrantProperties properties;

    public QdrantCollectionInitializer(RestClient restClient, QdrantProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (collectionExists()) {
            return;
        }

        restClient.put()
                .uri("/collections/{collectionName}", properties.collectionName())
                .headers(this::applyApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "vectors", Map.of(
                                "size", properties.vectorSize(),
                                "distance", normalizeDistance(properties.distance())
                        )
                ))
                .retrieve()
                .toBodilessEntity();
    }

    private boolean collectionExists() {
        try {
            restClient.get()
                    .uri("/collections/{collectionName}", properties.collectionName())
                    .headers(this::applyApiKey)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return false;
            }
            throw exception;
        }
    }

    private void applyApiKey(HttpHeaders headers) {
        if (StringUtils.hasText(properties.apiKey())) {
            headers.set("api-key", properties.apiKey());
        }
    }

    private String normalizeDistance(String distance) {
        if (!StringUtils.hasText(distance)) {
            return "Cosine";
        }
        String normalized = distance.trim().toUpperCase();
        return switch (normalized) {
            case "COSINE" -> "Cosine";
            case "DOT" -> "Dot";
            case "EUCLID" -> "Euclid";
            case "MANHATTAN" -> "Manhattan";
            default -> distance;
        };
    }
}
