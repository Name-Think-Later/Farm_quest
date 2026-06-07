package nutc.sot.farm_quest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "nutc.sot.farm_quest.infrastructure.redis")
public class RedisConfig {
}
