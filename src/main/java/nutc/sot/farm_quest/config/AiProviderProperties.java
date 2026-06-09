package nutc.sot.farm_quest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ai.provider")
public class AiProviderProperties {

    private String name = "openai";
    private String apiKey;
}
