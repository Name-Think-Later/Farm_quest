package nutc.sot.farm_quest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "farm-quest.qdrant")
public record QdrantProperties(
        boolean enabled,
        String url,
        String apiKey,
        String collectionName,
        int vectorSize,
        String distance
) {
}
