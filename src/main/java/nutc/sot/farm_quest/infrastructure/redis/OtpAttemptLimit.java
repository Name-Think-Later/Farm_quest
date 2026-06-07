package nutc.sot.farm_quest.infrastructure.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;

@RedisHash("otp_attempt_limit")
public class OtpAttemptLimit {

    @Id
    private String key;
    private int attemptCount;
    private Instant expiresAt;
    @TimeToLive
    private Long ttlSeconds;

    public OtpAttemptLimit() {
    }

    public OtpAttemptLimit(String key, int attemptCount, Instant expiresAt, Long ttlSeconds) {
        this.key = key;
        this.attemptCount = attemptCount;
        this.expiresAt = expiresAt;
        this.ttlSeconds = ttlSeconds;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
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
