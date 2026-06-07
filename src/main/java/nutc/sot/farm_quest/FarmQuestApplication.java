package nutc.sot.farm_quest;

import nutc.sot.farm_quest.config.AiProviderProperties;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.config.DatasourceProperties;
import nutc.sot.farm_quest.config.QdrantProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({QdrantProperties.class, AiProviderProperties.class, DatasourceProperties.class, AuthProperties.class})
public class FarmQuestApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmQuestApplication.class, args);
    }
}
