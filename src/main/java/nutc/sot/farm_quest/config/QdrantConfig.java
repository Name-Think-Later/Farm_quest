package nutc.sot.farm_quest.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(QdrantProperties.class)
public class QdrantConfig {

    @Bean
    RestClient qdrantRestClient(QdrantProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.url())
                .build();
    }
}
