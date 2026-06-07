package nutc.sot.farm_quest.service.system;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.DriverManager;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AiProviderProperties;
import nutc.sot.farm_quest.config.DatasourceProperties;
import nutc.sot.farm_quest.config.QdrantProperties;
import nutc.sot.farm_quest.dto.system.DependencyItemResponse;
import nutc.sot.farm_quest.dto.system.DependencyStatusResponse;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class DependencyCheckService {

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final String STATUS_CONFIGURED = "CONFIGURED";
    private static final String STATUS_MISSING = "MISSING";

    private final RedisConnectionFactory redisConnectionFactory;
    private final DatasourceProperties datasourceProperties;
    private final QdrantProperties qdrantProperties;
    private final AiProviderProperties aiProviderProperties;
    private final RestClient.Builder restClientBuilder;

    public DependencyStatusResponse checkDependencies() {
        List<DependencyItemResponse> dependencies = List.of(
                checkPostgreSql(),
                checkRedis(),
                checkQdrant(),
                checkAiProvider()
        );

        boolean allHealthy = dependencies.stream()
                .map(DependencyItemResponse::status)
                .allMatch(status -> STATUS_UP.equals(status) || STATUS_CONFIGURED.equals(status));

        return new DependencyStatusResponse(
                allHealthy ? STATUS_UP : STATUS_DOWN,
                OffsetDateTime.now(),
                dependencies
        );
    }

    private DependencyItemResponse checkPostgreSql() {
        if (!StringUtils.hasText(datasourceProperties.getUrl())) {
            return new DependencyItemResponse("PostgreSQL", STATUS_MISSING, "Datasource URL is not configured");
        }

        try (var connection = DriverManager.getConnection(
                datasourceProperties.getUrl(),
                datasourceProperties.getUsername(),
                datasourceProperties.getPassword()
        )) {
            return new DependencyItemResponse("PostgreSQL", STATUS_UP, "Connection successful");
        } catch (Exception ex) {
            return new DependencyItemResponse("PostgreSQL", STATUS_DOWN, sanitizeMessage(ex));
        }
    }

    private DependencyItemResponse checkRedis() {
        try (var connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            if ("PONG".equalsIgnoreCase(pong)) {
                return new DependencyItemResponse("Redis", STATUS_UP, "Connection successful");
            }
            return new DependencyItemResponse("Redis", STATUS_DOWN, "Unexpected ping response");
        } catch (Exception ex) {
            return new DependencyItemResponse("Redis", STATUS_DOWN, sanitizeMessage(ex));
        }
    }

    private DependencyItemResponse checkQdrant() {
        try {
            restClientBuilder
                    .baseUrl(qdrantProperties.getUrl())
                    .build()
                    .get()
                    .uri("/collections/{collectionName}", qdrantProperties.getCollectionName())
                    .retrieve()
                    .toBodilessEntity();
            return new DependencyItemResponse("Qdrant", STATUS_UP, "Collection reachable");
        } catch (Exception ex) {
            return new DependencyItemResponse("Qdrant", STATUS_DOWN, sanitizeMessage(ex));
        }
    }

    private DependencyItemResponse checkAiProvider() {
        boolean hasName = StringUtils.hasText(aiProviderProperties.getName());
        boolean hasApiKey = StringUtils.hasText(aiProviderProperties.getApiKey());
        if (hasName && hasApiKey) {
            return new DependencyItemResponse("Spring AI Provider", STATUS_CONFIGURED, "Provider configuration present");
        }
        return new DependencyItemResponse("Spring AI Provider", STATUS_MISSING, "Provider configuration incomplete");
    }

    private String sanitizeMessage(Exception exception) {
        String message = exception.getMessage();
        if (!StringUtils.hasText(message)) {
            return "Dependency check failed";
        }

        String sanitized = message
                .replaceAll("(?i)password=[^\\s;&]+", "password=***")
                .replaceAll("(?i)api[-_ ]?key=[^\\s;&]+", "apiKey=***")
                .replaceAll("jdbc:[^\\s]+", "jdbc:***")
                .replaceAll("https?://[^\\s]+", "url=***");

        if (containsAuthority(message)) {
            return "Dependency check failed";
        }

        return sanitized.length() > 180 ? sanitized.substring(0, 180) : sanitized;
    }

    private boolean containsAuthority(String message) {
        try {
            URI uri = new URI(message);
            return uri.getAuthority() != null;
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}
