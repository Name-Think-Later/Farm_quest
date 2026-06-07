package nutc.sot.farm_quest.infrastructure.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;
import java.util.UUID;

@RedisHash("visitor_session_cache")
public class VisitorSessionCache {

    @Id
    private String tokenHash;
    private UUID visitorSessionId;
    private UUID visitorAccountId;
    private UUID gameId;
    private String status;
    private Instant expiresAt;
    @TimeToLive
    private Long ttlSeconds;

    public VisitorSessionCache() {
    }

    public VisitorSessionCache(String tokenHash, UUID visitorSessionId, UUID visitorAccountId, UUID gameId, String status, Instant expiresAt, Long ttlSeconds) {
        this.tokenHash = tokenHash;
        this.visitorSessionId = visitorSessionId;
        this.visitorAccountId = visitorAccountId;
        this.gameId = gameId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.ttlSeconds = ttlSeconds;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public UUID getVisitorSessionId() {
        return visitorSessionId;
    }

    public void setVisitorSessionId(UUID visitorSessionId) {
        this.visitorSessionId = visitorSessionId;
    }

    public UUID getVisitorAccountId() {
        return visitorAccountId;
    }

    public void setVisitorAccountId(UUID visitorAccountId) {
        this.visitorAccountId = visitorAccountId;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
