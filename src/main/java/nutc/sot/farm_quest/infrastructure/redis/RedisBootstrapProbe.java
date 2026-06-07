package nutc.sot.farm_quest.infrastructure.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;

@RedisHash("bootstrap_probe")
public class RedisBootstrapProbe {

    @Id
    private String id;
    private Instant touchedAt;

    public RedisBootstrapProbe() {
    }

    public RedisBootstrapProbe(String id, Instant touchedAt) {
        this.id = id;
        this.touchedAt = touchedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTouchedAt() {
        return touchedAt;
    }

    public void setTouchedAt(Instant touchedAt) {
        this.touchedAt = touchedAt;
    }
}
