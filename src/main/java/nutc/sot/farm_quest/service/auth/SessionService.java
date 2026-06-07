package nutc.sot.farm_quest.service.auth;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.auth.CurrentUserResponse;
import nutc.sot.farm_quest.dto.auth.LogoutResponse;
import nutc.sot.farm_quest.dto.auth.VisitorSessionResponse;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import nutc.sot.farm_quest.infrastructure.redis.VisitorSessionCache;
import nutc.sot.farm_quest.infrastructure.redis.VisitorSessionCacheRepository;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.persistence.repository.VisitorSessionRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final VisitorSessionRepository visitorSessionRepository;
    private final AuthSecurityService authSecurityService;
    private final AuthProperties authProperties;
    private final VisitorAuthService visitorAuthService;
    private final ObjectProvider<VisitorSessionCacheRepository> visitorSessionCacheRepositoryProvider;

    @Transactional
    public VisitorSessionResponse createSession(VisitorAccountEntity visitorAccount, String clientIp, String userAgent) {
        OffsetDateTime now = OffsetDateTime.now();
        String sessionToken = authSecurityService.generateSessionToken();
        String tokenHash = authSecurityService.hashSessionToken(sessionToken, authProperties.getSessionSecret());

        VisitorSessionEntity session = new VisitorSessionEntity();
        session.setId(UUID.randomUUID());
        session.setGame(visitorAccount.getGame());
        session.setVisitorAccount(visitorAccount);
        session.setTokenHash(tokenHash);
        session.setStatus("ACTIVE");
        session.setIssuedAt(now);
        session.setExpiresAt(now.plusHours(authProperties.getSessionHours()));
        session.setLastSeenAt(now);
        session.setClientIp(truncate(clientIp, 64));
        session.setUserAgent(truncate(userAgent, 512));
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        VisitorSessionEntity savedSession = visitorSessionRepository.save(session);
        cacheSession(savedSession, tokenHash);

        return new VisitorSessionResponse(
                visitorAccount.getId(),
                visitorAccount.getEmailNormalized(),
                sessionToken,
                savedSession.getIssuedAt(),
                savedSession.getExpiresAt(),
                true
        );
    }

    @Transactional
    public VisitorSessionEntity requireActiveSession(String token) {
        if (!StringUtils.hasText(token)) {
            throw invalidSession();
        }

        String tokenHash = authSecurityService.hashSessionToken(token, authProperties.getSessionSecret());
        VisitorSessionCache cachedSession = getCachedSession(tokenHash);
        if (cachedSession != null) {
            if (!"ACTIVE".equals(cachedSession.getStatus())) {
                evictSessionCache(tokenHash);
                throw invalidSession();
            }
            if (cachedSession.getExpiresAt() != null && cachedSession.getExpiresAt().isBefore(Instant.now())) {
                evictSessionCache(tokenHash);
            } else {
                VisitorSessionEntity cachedDatabaseSession = visitorSessionRepository.findById(cachedSession.getVisitorSessionId())
                        .orElse(null);
                if (cachedDatabaseSession != null) {
                    return validateAndTouchSession(cachedDatabaseSession, tokenHash);
                }
                evictSessionCache(tokenHash);
            }
        }

        VisitorSessionEntity session = visitorSessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(this::invalidSession);
        return validateAndTouchSession(session, tokenHash);
    }

    @Transactional
    public CurrentUserResponse getCurrentUser(String token) {
        VisitorSessionEntity session = requireActiveSession(token);
        return visitorAuthService.toCurrentUserResponse(session.getVisitorAccount(), session.getExpiresAt());
    }

    @Transactional
    public LogoutResponse logout(String token) {
        if (!StringUtils.hasText(token)) {
            return new LogoutResponse(true);
        }

        String tokenHash = authSecurityService.hashSessionToken(token, authProperties.getSessionSecret());
        visitorSessionRepository.findByTokenHash(tokenHash)
                .ifPresent(session -> {
                    if ("ACTIVE".equals(session.getStatus())) {
                        OffsetDateTime now = OffsetDateTime.now();
                        session.setStatus("LOGGED_OUT");
                        session.setRevokedAt(now);
                        session.setUpdatedAt(now);
                        visitorSessionRepository.save(session);
                    }
                });
        evictSessionCache(tokenHash);
        return new LogoutResponse(true);
    }

    private VisitorSessionEntity validateAndTouchSession(VisitorSessionEntity session, String tokenHash) {
        if (!"ACTIVE".equals(session.getStatus())) {
            evictSessionCache(tokenHash);
            throw invalidSession();
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (session.getExpiresAt().isBefore(now)) {
            session.setStatus("EXPIRED");
            session.setUpdatedAt(now);
            visitorSessionRepository.save(session);
            evictSessionCache(tokenHash);
            throw new AuthException(AuthErrorCode.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED, "Session has expired");
        }

        session.setLastSeenAt(now);
        session.setUpdatedAt(now);
        VisitorSessionEntity savedSession = visitorSessionRepository.save(session);
        cacheSession(savedSession, tokenHash);
        return savedSession;
    }

    private VisitorSessionCache getCachedSession(String tokenHash) {
        VisitorSessionCacheRepository repository = visitorSessionCacheRepositoryProvider.getIfAvailable();
        if (repository == null) {
            return null;
        }
        return repository.findById(tokenHash).orElse(null);
    }

    private void cacheSession(VisitorSessionEntity session, String tokenHash) {
        VisitorSessionCacheRepository repository = visitorSessionCacheRepositoryProvider.getIfAvailable();
        if (repository == null) {
            return;
        }

        long ttlSeconds = Math.max(1L, Duration.between(Instant.now(), session.getExpiresAt().toInstant()).getSeconds());
        repository.save(new VisitorSessionCache(
                tokenHash,
                session.getId(),
                session.getVisitorAccount().getId(),
                session.getGame().getId(),
                session.getStatus(),
                session.getExpiresAt().toInstant(),
                ttlSeconds
        ));
    }

    private void evictSessionCache(String tokenHash) {
        VisitorSessionCacheRepository repository = visitorSessionCacheRepositoryProvider.getIfAvailable();
        if (repository != null) {
            repository.deleteById(tokenHash);
        }
    }

    private AuthException invalidSession() {
        return new AuthException(AuthErrorCode.SESSION_INVALID, HttpStatus.UNAUTHORIZED, "Session token is invalid");
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
