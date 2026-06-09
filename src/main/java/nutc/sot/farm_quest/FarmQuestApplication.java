package nutc.sot.farm_quest;

import nutc.sot.farm_quest.config.AdminAuthProperties;
import nutc.sot.farm_quest.config.AiProviderProperties;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.config.DatasourceProperties;
import nutc.sot.farm_quest.config.QdrantProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({QdrantProperties.class, AiProviderProperties.class, DatasourceProperties.class, AuthProperties.class, AdminAuthProperties.class})
public class FarmQuestApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmQuestApplication.class, args);
    }
}
