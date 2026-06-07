package nutc.sot.farm_quest.service.auth;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import nutc.sot.farm_quest.infrastructure.redis.OtpAttemptLimit;
import nutc.sot.farm_quest.infrastructure.redis.OtpAttemptLimitRepository;
import nutc.sot.farm_quest.infrastructure.redis.OtpResendCooldown;
import nutc.sot.farm_quest.infrastructure.redis.OtpResendCooldownRepository;
import nutc.sot.farm_quest.persistence.entity.EmailVerificationEntity;
import nutc.sot.farm_quest.persistence.repository.EmailVerificationRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final AuthProperties authProperties;
    private final ObjectProvider<OtpResendCooldownRepository> otpResendCooldownRepositoryProvider;
    private final ObjectProvider<OtpAttemptLimitRepository> otpAttemptLimitRepositoryProvider;

    public void checkRequestAllowed(String emailNormalized) {
        OffsetDateTime now = OffsetDateTime.now();
        checkRedisCooldown(emailNormalized, now);

        emailVerificationRepository.findFirstByEmailNormalizedAndStatusOrderByRequestedAtDesc(emailNormalized, "PENDING")
                .ifPresent(verification -> {
                    OffsetDateTime availableAt = verification.getRequestedAt()
                            .plusSeconds(authProperties.getOtpResendCooldownSeconds());
                    if (availableAt.isAfter(now)) {
                        throw resendCooldownRateLimited();
                    }
                });

        long requestsInLastHour = emailVerificationRepository.countByEmailNormalizedAndRequestedAtAfter(
                emailNormalized,
                now.minusHours(1)
        );
        if (requestsInLastHour >= authProperties.getOtpMaxRequestsPerHour()) {
            throw hourlyRequestLimitRateLimited();
        }
    }

    public void recordVerificationRequested(String emailNormalized, OffsetDateTime resendAvailableAt) {
        OtpResendCooldownRepository repository = otpResendCooldownRepositoryProvider.getIfAvailable();
        if (repository == null) {
            return;
        }

        long ttlSeconds = secondsUntil(resendAvailableAt);
        repository.save(new OtpResendCooldown(
                buildCooldownKey(emailNormalized),
                resendAvailableAt.toInstant(),
                ttlSeconds
        ));
    }

    public void checkAttemptsAllowed(EmailVerificationEntity verification) {
        int effectiveAttemptCount = Math.max(verification.getAttemptCount(), readRedisAttemptCount(verification));
        if (effectiveAttemptCount >= authProperties.getOtpMaxAttempts()) {
            throw new AuthException(
                    AuthErrorCode.EMAIL_VERIFICATION_INVALID,
                    HttpStatus.BAD_REQUEST,
                    "OTP is invalid"
            );
        }
    }

    public int registerFailedAttempt(EmailVerificationEntity verification) {
        OtpAttemptLimitRepository repository = otpAttemptLimitRepositoryProvider.getIfAvailable();
        if (repository == null) {
            return verification.getAttemptCount() + 1;
        }

        String key = buildAttemptKey(verification);
        int newAttemptCount = repository.findById(key)
                .map(OtpAttemptLimit::getAttemptCount)
                .orElse(verification.getAttemptCount()) + 1;

        repository.save(new OtpAttemptLimit(
                key,
                newAttemptCount,
                verification.getExpiresAt().toInstant(),
                secondsUntil(verification.getExpiresAt())
        ));
        return newAttemptCount;
    }

    private void checkRedisCooldown(String emailNormalized, OffsetDateTime now) {
        OtpResendCooldownRepository repository = otpResendCooldownRepositoryProvider.getIfAvailable();
        if (repository == null) {
            return;
        }

        repository.findById(buildCooldownKey(emailNormalized)).ifPresent(cooldown -> {
            if (cooldown.getExpiresAt() != null && cooldown.getExpiresAt().isAfter(now.toInstant())) {
                throw resendCooldownRateLimited();
            }
            repository.deleteById(cooldown.getKey());
        });
    }

    private int readRedisAttemptCount(EmailVerificationEntity verification) {
        OtpAttemptLimitRepository repository = otpAttemptLimitRepositoryProvider.getIfAvailable();
        if (repository == null) {
            return verification.getAttemptCount();
        }

        return repository.findById(buildAttemptKey(verification))
                .map(OtpAttemptLimit::getAttemptCount)
                .orElse(verification.getAttemptCount());
    }

    private long secondsUntil(OffsetDateTime expiresAt) {
        return secondsUntil(expiresAt.toInstant());
    }

    private long secondsUntil(Instant expiresAt) {
        return Math.max(1L, Duration.between(Instant.now(), expiresAt).getSeconds());
    }

    private String buildCooldownKey(String emailNormalized) {
        return "otp_resend:" + emailNormalized;
    }

    private String buildAttemptKey(EmailVerificationEntity verification) {
        return "otp_attempt:" + verification.getId();
    }

    private AuthException resendCooldownRateLimited() {
        return new AuthException(
                AuthErrorCode.EMAIL_VERIFICATION_RATE_LIMITED,
                HttpStatus.TOO_MANY_REQUESTS,
                "OTP resend cooldown is active"
        );
    }

    private AuthException hourlyRequestLimitRateLimited() {
        return new AuthException(
                AuthErrorCode.EMAIL_VERIFICATION_RATE_LIMITED,
                HttpStatus.TOO_MANY_REQUESTS,
                "OTP hourly request limit exceeded"
        );
    }
}
