package nutc.sot.farm_quest.service.auth;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.exception.AuthException;
import nutc.sot.farm_quest.infrastructure.redis.OtpAttemptLimit;
import nutc.sot.farm_quest.infrastructure.redis.OtpAttemptLimitRepository;
import nutc.sot.farm_quest.infrastructure.redis.OtpResendCooldown;
import nutc.sot.farm_quest.infrastructure.redis.OtpResendCooldownRepository;
import nutc.sot.farm_quest.persistence.entity.EmailVerificationEntity;
import nutc.sot.farm_quest.persistence.repository.EmailVerificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

class RateLimitServiceTest {

    private final EmailVerificationRepository emailVerificationRepository = mock(EmailVerificationRepository.class);
    private final OtpResendCooldownRepository otpResendCooldownRepository = mock(OtpResendCooldownRepository.class);
    private final OtpAttemptLimitRepository otpAttemptLimitRepository = mock(OtpAttemptLimitRepository.class);
    private final ObjectProvider<OtpResendCooldownRepository> otpResendCooldownRepositoryProvider = mock(ObjectProvider.class);
    private final ObjectProvider<OtpAttemptLimitRepository> otpAttemptLimitRepositoryProvider = mock(ObjectProvider.class);
    private final AuthProperties authProperties = new AuthProperties();
    private final RateLimitService rateLimitService = new RateLimitService(
            emailVerificationRepository,
            authProperties,
            otpResendCooldownRepositoryProvider,
            otpAttemptLimitRepositoryProvider
    );

    @Test
    void checkRequestAllowedBlocksWhenRedisCooldownExists() {
        authProperties.setOtpMaxRequestsPerHour(5);
        when(otpResendCooldownRepositoryProvider.getIfAvailable()).thenReturn(otpResendCooldownRepository);
        when(otpAttemptLimitRepositoryProvider.getIfAvailable()).thenReturn(otpAttemptLimitRepository);
        when(otpResendCooldownRepository.findById("otp_resend:test@example.com")).thenReturn(Optional.of(
                new OtpResendCooldown("otp_resend:test@example.com", Instant.now().plusSeconds(30), 30L)
        ));

        assertThatThrownBy(() -> rateLimitService.checkRequestAllowed("test@example.com"))
                .isInstanceOf(AuthException.class)
                .hasMessage("OTP resend cooldown is active");
    }

    @Test
    void checkRequestAllowedBlocksWhenHourlyLimitReached() {
        authProperties.setOtpMaxRequestsPerHour(2);
        when(otpResendCooldownRepositoryProvider.getIfAvailable()).thenReturn(otpResendCooldownRepository);
        when(otpResendCooldownRepository.findById("otp_resend:test@example.com")).thenReturn(Optional.empty());
        when(emailVerificationRepository.findFirstByEmailNormalizedAndStatusOrderByRequestedAtDesc("test@example.com", "PENDING"))
                .thenReturn(Optional.empty());
        when(emailVerificationRepository.countByEmailNormalizedAndRequestedAtAfter(
                org.mockito.ArgumentMatchers.eq("test@example.com"),
                org.mockito.ArgumentMatchers.any(OffsetDateTime.class)
        )).thenReturn(2L);

        assertThatThrownBy(() -> rateLimitService.checkRequestAllowed("test@example.com"))
                .isInstanceOf(AuthException.class)
                .hasMessage("OTP hourly request limit exceeded");
        verify(otpResendCooldownRepository, never()).deleteById("otp_resend:test@example.com");
    }

    @Test
    void checkAttemptsAllowedBlocksWhenRedisAttemptsReachLimit() {
        authProperties.setOtpMaxAttempts(5);
        EmailVerificationEntity verification = new EmailVerificationEntity();
        verification.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        verification.setAttemptCount(0);

        when(otpAttemptLimitRepositoryProvider.getIfAvailable()).thenReturn(otpAttemptLimitRepository);
        when(otpAttemptLimitRepository.findById("otp_attempt:aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")).thenReturn(Optional.of(
                new OtpAttemptLimit("otp_attempt:aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", 5, Instant.now().plusSeconds(60), 60L)
        ));

        assertThatThrownBy(() -> rateLimitService.checkAttemptsAllowed(verification))
                .isInstanceOf(AuthException.class)
                .hasMessage("OTP is invalid");
    }

    @Test
    void registerFailedAttemptStoresRedisCounter() {
        EmailVerificationEntity verification = new EmailVerificationEntity();
        verification.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        verification.setAttemptCount(0);
        verification.setExpiresAt(OffsetDateTime.now().plusMinutes(10));

        when(otpAttemptLimitRepositoryProvider.getIfAvailable()).thenReturn(otpAttemptLimitRepository);
        when(otpAttemptLimitRepository.findById("otp_attempt:aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")).thenReturn(Optional.empty());

        int attemptCount = rateLimitService.registerFailedAttempt(verification);

        assertThat(attemptCount).isEqualTo(1);
        verify(otpAttemptLimitRepository).save(argThat(limit ->
                "otp_attempt:aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa".equals(limit.getKey())
                        && limit.getAttemptCount() == 1
                        && limit.getTtlSeconds() > 0
        ));
    }
}
