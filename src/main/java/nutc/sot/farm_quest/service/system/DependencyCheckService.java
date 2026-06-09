package nutc.sot.farm_quest.service.system;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.QdrantProperties;
import nutc.sot.farm_quest.dto.system.DependencyItemResponse;
import nutc.sot.farm_quest.dto.system.DependencyStatusResponse;
import nutc.sot.farm_quest.service.quest.EmbeddingService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
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

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final QdrantProperties qdrantProperties;
    @Qualifier("qdrantRestClient")
    private final RestClient qdrantRestClient;
    private final Environment environment;
    private final ChatClient chatClient;
    private final EmbeddingService embeddingService;

    public DependencyStatusResponse checkDependencies() {
        List<DependencyItemResponse> dependencies = List.of(
                checkPostgreSql(),
                checkRedis(),
                checkQdrant(),
                checkAiChatModel(),
                checkAiEmbeddingModel()
        );

        return new DependencyStatusResponse(
                overallStatus(dependencies, STATUS_UP, STATUS_CONFIGURED),
                OffsetDateTime.now(),
                dependencies
        );
    }

    public DependencyStatusResponse probeAiDependencies() {
        List<DependencyItemResponse> dependencies = List.of(
                probeAiChatModel(),
                probeAiEmbeddingModel()
        );

        return new DependencyStatusResponse(
                overallStatus(dependencies, STATUS_UP),
                OffsetDateTime.now(),
                dependencies
        );
    }

    private DependencyItemResponse checkPostgreSql() {
        try (var connection = dataSource.getConnection()) {
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
        if (!qdrantProperties.enabled()) {
            return new DependencyItemResponse("Qdrant", STATUS_MISSING, "Qdrant is disabled");
        }

        try {
            qdrantRestClient
                    .get()
                    .uri("/collections/{collectionName}", qdrantProperties.collectionName())
                    .retrieve()
                    .toBodilessEntity();
            return new DependencyItemResponse("Qdrant", STATUS_UP, "Collection reachable");
        } catch (Exception ex) {
            return new DependencyItemResponse("Qdrant", STATUS_DOWN, sanitizeMessage(ex));
        }
    }

    private DependencyItemResponse checkAiChatModel() {
        String provider = chatProvider();
        String model = chatModel();
        String apiKey = chatApiKey();
        String baseUrl = chatBaseUrl();

        if (StringUtils.hasText(provider) && StringUtils.hasText(model) && StringUtils.hasText(apiKey) && StringUtils.hasText(baseUrl)) {
            return new DependencyItemResponse("AI Chat Model", STATUS_CONFIGURED, "Provider=" + provider + ", model=" + model);
        }
        return new DependencyItemResponse("AI Chat Model", STATUS_MISSING, "Chat model configuration incomplete");
    }

    private DependencyItemResponse checkAiEmbeddingModel() {
        String provider = embeddingProvider();
        String model = embeddingModel();
        String apiKey = embeddingApiKey();
        String baseUrl = embeddingBaseUrl();

        if (StringUtils.hasText(provider) && StringUtils.hasText(model) && StringUtils.hasText(apiKey) && StringUtils.hasText(baseUrl)) {
            return new DependencyItemResponse("AI Embedding Model", STATUS_CONFIGURED, "Provider=" + provider + ", model=" + model);
        }
        return new DependencyItemResponse("AI Embedding Model", STATUS_MISSING, "Embedding model configuration incomplete");
    }

    private DependencyItemResponse probeAiChatModel() {
        DependencyItemResponse configured = checkAiChatModel();
        if (!STATUS_CONFIGURED.equals(configured.status())) {
            return new DependencyItemResponse("AI Chat Model", STATUS_MISSING, configured.message());
        }

        try {
            String response = chatClient.prompt()
                    .user("reply with ok")
                    .call()
                    .content();
            if (StringUtils.hasText(response)) {
                return new DependencyItemResponse("AI Chat Model", STATUS_UP, "Provider=" + chatProvider() + ", model=" + chatModel());
            }
            return new DependencyItemResponse("AI Chat Model", STATUS_DOWN, "Chat model returned empty content");
        } catch (RuntimeException ex) {
            return new DependencyItemResponse("AI Chat Model", STATUS_DOWN, sanitizeMessage(ex));
        }
    }

    private DependencyItemResponse probeAiEmbeddingModel() {
        DependencyItemResponse configured = checkAiEmbeddingModel();
        if (!STATUS_CONFIGURED.equals(configured.status())) {
            return new DependencyItemResponse("AI Embedding Model", STATUS_MISSING, configured.message());
        }

        try {
            float[] vector = embeddingService.model().embed("farm quest probe");
            if (vector != null && vector.length > 0) {
                return new DependencyItemResponse("AI Embedding Model", STATUS_UP, "Provider=" + embeddingProvider() + ", model=" + embeddingModel());
            }
            return new DependencyItemResponse("AI Embedding Model", STATUS_DOWN, "Embedding model returned empty vector");
        } catch (RuntimeException ex) {
            return new DependencyItemResponse("AI Embedding Model", STATUS_DOWN, sanitizeMessage(ex));
        }
    }

    private String overallStatus(List<DependencyItemResponse> dependencies, String... healthyStatuses) {
        return dependencies.stream()
                .map(DependencyItemResponse::status)
                .allMatch(status -> List.of(healthyStatuses).contains(status)) ? STATUS_UP : STATUS_DOWN;
    }

    private String chatProvider() {
        return firstPresent(
                environment.getProperty("spring.ai.chat.provider"),
                environment.getProperty("SPRING_AI_CHAT_PROVIDER"),
                environment.getProperty("spring.ai.model.chat")
        );
    }

    private String chatModel() {
        return firstPresent(
                environment.getProperty("spring.ai.openai.chat.model"),
                environment.getProperty("spring.ai.openai.chat.options.model"),
                environment.getProperty("SPRING_AI_CHAT_MODEL")
        );
    }

    private String chatApiKey() {
        return firstPresent(
                environment.getProperty("PROXY_API_KEY"),
                environment.getProperty("spring.ai.openai.api-key")
        );
    }

    private String chatBaseUrl() {
        return firstPresent(
                environment.getProperty("PROXY_BASE_URL"),
                environment.getProperty("spring.ai.openai.base-url")
        );
    }

    private String embeddingProvider() {
        return firstPresent(
                environment.getProperty("spring.ai.embedding.provider"),
                environment.getProperty("SPRING_AI_EMBEDDING_PROVIDER"),
                environment.getProperty("spring.ai.model.embedding.text")
        );
    }

    private String embeddingModel() {
        return firstPresent(
                environment.getProperty("spring.ai.openai.embedding.model"),
                environment.getProperty("spring.ai.openai.embedding.options.model"),
                environment.getProperty("SPRING_AI_EMBEDDING_MODEL")
        );
    }

    private String embeddingApiKey() {
        return firstPresent(
                environment.getProperty("EMBEDDING_API_KEY"),
                environment.getProperty("PROXY_API_KEY"),
                environment.getProperty("spring.ai.openai.embedding.api-key")
        );
    }

    private String embeddingBaseUrl() {
        return firstPresent(
                environment.getProperty("EMBEDDING_BASE_URL"),
                environment.getProperty("PROXY_BASE_URL"),
                environment.getProperty("spring.ai.openai.embedding.base-url")
        );
    }

    private String firstPresent(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
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
