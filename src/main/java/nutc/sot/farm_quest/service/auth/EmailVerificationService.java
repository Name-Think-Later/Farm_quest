package nutc.sot.farm_quest.service.auth;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.auth.ConfirmEmailVerificationRequest;
import nutc.sot.farm_quest.dto.auth.EmailVerificationRequest;
import nutc.sot.farm_quest.dto.auth.EmailVerificationResponse;
import nutc.sot.farm_quest.dto.auth.VisitorSessionResponse;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import nutc.sot.farm_quest.persistence.entity.EmailVerificationEntity;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.repository.EmailVerificationRepository;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final GameRepository gameRepository;
    private final VisitorAuthService visitorAuthService;
    private final RateLimitService rateLimitService;
    private final AuthSecurityService authSecurityService;
    private final SessionService sessionService;
    private final MailService mailService;
    private final AuthProperties authProperties;

    @Transactional
    public EmailVerificationResponse createVerification(EmailVerificationRequest request, String clientIp, String userAgent) {
        String normalizedEmail = normalizeRequiredEmail(request.email());
        rateLimitService.checkRequestAllowed(normalizedEmail);

        VisitorAccountEntity visitorAccount = visitorAuthService.getOrCreateVisitor(normalizedEmail);
        revokePendingVerifications(normalizedEmail);

        String otp = authSecurityService.generateOtp();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime resendAvailableAt = now.plusSeconds(authProperties.getOtpResendCooldownSeconds());
        EmailVerificationEntity verification = new EmailVerificationEntity();
        verification.setId(UUID.randomUUID());
        verification.setGame(resolveGame());
        verification.setVisitorAccount(visitorAccount);
        verification.setEmailNormalized(normalizedEmail);
        verification.setOtpHash(authSecurityService.hashOtp(
                normalizedEmail,
                otp,
                authProperties.getEmailVerificationSecret()
        ));
        verification.setStatus("PENDING");
        verification.setRequestedAt(now);
        verification.setExpiresAt(now.plusMinutes(authProperties.getOtpExpiryMinutes()));
        verification.setAttemptCount(0);
        verification.setClientIp(clientIp);
        verification.setUserAgent(truncate(userAgent, 512));
        verification.setCreatedAt(now);

        emailVerificationRepository.save(verification);
        rateLimitService.recordVerificationRequested(normalizedEmail, resendAvailableAt);
        mailService.sendOtp(normalizedEmail, otp);

        return new EmailVerificationResponse(
                normalizedEmail,
                verification.getExpiresAt(),
                resendAvailableAt,
                verification.getStatus()
        );
    }

    @Transactional
    public VisitorSessionResponse confirmVerification(ConfirmEmailVerificationRequest request, String clientIp, String userAgent) {
        String normalizedEmail = normalizeRequiredEmail(request.email());
        EmailVerificationEntity verification = emailVerificationRepository
                .findFirstByEmailNormalizedAndStatusOrderByRequestedAtDesc(normalizedEmail, "PENDING")
                .orElseThrow(() -> new AuthException(
                        AuthErrorCode.EMAIL_VERIFICATION_INVALID,
                        HttpStatus.BAD_REQUEST,
                        "OTP is invalid"
                ));

        if (verification.getExpiresAt().isBefore(OffsetDateTime.now())) {
            verification.setStatus("EXPIRED");
            emailVerificationRepository.save(verification);
            throw new AuthException(
                    AuthErrorCode.EMAIL_VERIFICATION_EXPIRED,
                    HttpStatus.BAD_REQUEST,
                    "OTP has expired"
            );
        }

        rateLimitService.checkAttemptsAllowed(verification);

        String expectedHash = authSecurityService.hashOtp(
                normalizedEmail,
                request.otp(),
                authProperties.getEmailVerificationSecret()
        );
        if (!expectedHash.equals(verification.getOtpHash())) {
            int latestAttemptCount = rateLimitService.registerFailedAttempt(verification);
            verification.setAttemptCount(Math.max(verification.getAttemptCount() + 1, latestAttemptCount));
            if (verification.getAttemptCount() >= authProperties.getOtpMaxAttempts()) {
                verification.setStatus("FAILED");
            }
            emailVerificationRepository.save(verification);
            throw new AuthException(
                    AuthErrorCode.EMAIL_VERIFICATION_INVALID,
                    HttpStatus.BAD_REQUEST,
                    "OTP is invalid"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();
        verification.setStatus("VERIFIED");
        verification.setVerifiedAt(now);
        emailVerificationRepository.save(verification);

        VisitorAccountEntity activeVisitor = visitorAuthService.activateVisitor(verification.getVisitorAccount());
        return sessionService.createSession(activeVisitor, clientIp, userAgent);
    }

    private void revokePendingVerifications(String normalizedEmail) {
        List<EmailVerificationEntity> pendingVerifications = emailVerificationRepository
                .findByEmailNormalizedAndStatus(normalizedEmail, "PENDING");
        for (EmailVerificationEntity pendingVerification : pendingVerifications) {
            pendingVerification.setStatus("REVOKED");
        }
        emailVerificationRepository.saveAll(pendingVerifications);
    }

    private GameEntity resolveGame() {
        return gameRepository.findByCode(authProperties.getGameCode())
                .orElseThrow(() -> new IllegalStateException("Configured game not found"));
    }

    private String normalizeRequiredEmail(String email) {
        String normalizedEmail = authSecurityService.normalizeEmail(email);
        if (!StringUtils.hasText(normalizedEmail)) {
            throw new AuthException(AuthErrorCode.INVALID_EMAIL, HttpStatus.BAD_REQUEST, "Email format is invalid");
        }
        return normalizedEmail;
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
