package nutc.sot.farm_quest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.datasource")
public class DatasourceProperties {

    private String url;
    private String username;
    private String password;
}
