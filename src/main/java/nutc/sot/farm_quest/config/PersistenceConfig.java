package nutc.sot.farm_quest.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(prefix = "farm-quest.persistence", name = "repositories.enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaRepositories(basePackages = "nutc.sot.farm_quest.persistence.repository")
public class PersistenceConfig {
}
