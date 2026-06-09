package nutc.sot.farm_quest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "farm-quest.admin")
public class AdminAuthProperties {

    private String authSecret;
}
