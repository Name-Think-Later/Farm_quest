package nutc.sot.farm_quest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "nutc.sot.farm_quest.persistence.repository")
public class PersistenceConfig {
}
