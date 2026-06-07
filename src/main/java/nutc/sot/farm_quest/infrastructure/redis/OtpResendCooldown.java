package nutc.sot.farm_quest.infrastructure.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;

@RedisHash("otp_resend_cooldown")
public class OtpResendCooldown {

    @Id
    private String key;
    private Instant expiresAt;
    @TimeToLive
    private Long ttlSeconds;

    public OtpResendCooldown() {
    }

    public OtpResendCooldown(String key, Instant expiresAt, Long ttlSeconds) {
        this.key = key;
        this.expiresAt = expiresAt;
        this.ttlSeconds = ttlSeconds;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
