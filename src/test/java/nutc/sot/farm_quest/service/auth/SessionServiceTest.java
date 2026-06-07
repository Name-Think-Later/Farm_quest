package nutc.sot.farm_quest.service.auth;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.infrastructure.redis.VisitorSessionCache;
import nutc.sot.farm_quest.infrastructure.redis.VisitorSessionCacheRepository;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.persistence.repository.VisitorSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionServiceTest {

    private final VisitorSessionRepository visitorSessionRepository = mock(VisitorSessionRepository.class);
    private final AuthSecurityService authSecurityService = mock(AuthSecurityService.class);
    private final VisitorAuthService visitorAuthService = mock(VisitorAuthService.class);
    private final VisitorSessionCacheRepository visitorSessionCacheRepository = mock(VisitorSessionCacheRepository.class);
    private final ObjectProvider<VisitorSessionCacheRepository> visitorSessionCacheRepositoryProvider = mock(ObjectProvider.class);
    private final AuthProperties authProperties = new AuthProperties();
    private final SessionService sessionService = new SessionService(
            visitorSessionRepository,
            authSecurityService,
            authProperties,
            visitorAuthService,
            visitorSessionCacheRepositoryProvider
    );

    @Test
    void createSessionCachesSession() {
        authProperties.setSessionSecret("session-secret");
        authProperties.setSessionHours(24);
        when(visitorSessionCacheRepositoryProvider.getIfAvailable()).thenReturn(visitorSessionCacheRepository);
        when(authSecurityService.generateSessionToken()).thenReturn("session-token");
        when(authSecurityService.hashSessionToken("session-token", "session-secret")).thenReturn("token-hash");
        when(visitorSessionRepository.save(any(VisitorSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameEntity game = new GameEntity();
        game.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        VisitorAccountEntity visitorAccount = new VisitorAccountEntity();
        visitorAccount.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        visitorAccount.setEmailNormalized("visitor@example.com");
        visitorAccount.setGame(game);

        var response = sessionService.createSession(visitorAccount, "127.0.0.1", "JUnit");

        assertThat(response.sessionToken()).isEqualTo("session-token");
        verify(visitorSessionCacheRepository).save(argThat(cache ->
                "token-hash".equals(cache.getTokenHash())
                        && visitorAccount.getId().equals(cache.getVisitorAccountId())
                        && game.getId().equals(cache.getGameId())
                        && "ACTIVE".equals(cache.getStatus())
        ));
    }

    @Test
    void requireActiveSessionUsesCacheLookupById() {
        authProperties.setSessionSecret("session-secret");
        when(visitorSessionCacheRepositoryProvider.getIfAvailable()).thenReturn(visitorSessionCacheRepository);
        when(authSecurityService.hashSessionToken("session-token", "session-secret")).thenReturn("token-hash");

        UUID sessionId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID accountId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID gameId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        when(visitorSessionCacheRepository.findById("token-hash")).thenReturn(Optional.of(
                new VisitorSessionCache("token-hash", sessionId, accountId, gameId, "ACTIVE", Instant.now().plusSeconds(60), 60L)
        ));

        GameEntity game = new GameEntity();
        game.setId(gameId);

        VisitorAccountEntity visitorAccount = new VisitorAccountEntity();
        visitorAccount.setId(accountId);
        visitorAccount.setEmailNormalized("visitor@example.com");
        visitorAccount.setGame(game);

        VisitorSessionEntity visitorSession = new VisitorSessionEntity();
        visitorSession.setId(sessionId);
        visitorSession.setGame(game);
        visitorSession.setVisitorAccount(visitorAccount);
        visitorSession.setTokenHash("token-hash");
        visitorSession.setStatus("ACTIVE");
        visitorSession.setIssuedAt(OffsetDateTime.now().minusMinutes(1));
        visitorSession.setExpiresAt(OffsetDateTime.now().plusHours(24));
        visitorSession.setCreatedAt(OffsetDateTime.now().minusMinutes(1));
        visitorSession.setUpdatedAt(OffsetDateTime.now().minusMinutes(1));

        when(visitorSessionRepository.findById(sessionId)).thenReturn(Optional.of(visitorSession));
        when(visitorSessionRepository.save(any(VisitorSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resolvedSession = sessionService.requireActiveSession("session-token");

        assertThat(resolvedSession.getId()).isEqualTo(sessionId);
        verify(visitorSessionRepository).findById(sessionId);
        verify(visitorSessionRepository, never()).findByTokenHash(anyString());
    }
}
