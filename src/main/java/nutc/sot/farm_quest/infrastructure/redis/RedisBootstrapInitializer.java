package nutc.sot.farm_quest.infrastructure.redis;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
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
