package nutc.sot.farm_quest;

import nutc.sot.farm_quest.config.AiProviderProperties;
import nutc.sot.farm_quest.config.DatasourceProperties;
import nutc.sot.farm_quest.config.QdrantProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@EnableConfigurationProperties({QdrantProperties.class, AiProviderProperties.class, DatasourceProperties.class})
public class FarmQuestApplication {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    public static void main(String[] args) {
        SpringApplication.run(FarmQuestApplication.class, args);
    }
}
