package nutc.sot.farm_quest.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@ConditionalOnProperty(prefix = "farm-quest.redis", name = "repositories.enabled", havingValue = "true", matchIfMissing = true)
@EnableRedisRepositories(basePackages = "nutc.sot.farm_quest.infrastructure.redis")
public class RedisConfig {
}
