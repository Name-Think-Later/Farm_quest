package nutc.sot.farm_quest.infrastructure.redis;

import java.time.Instant;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "farm-quest.redis", name = "bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class RedisBootstrapInitializer implements ApplicationRunner {

    private final RedisBootstrapProbeRepository repository;

    public RedisBootstrapInitializer(RedisBootstrapProbeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        repository.save(new RedisBootstrapProbe("stage2-bootstrap", Instant.now()));
    }
}
