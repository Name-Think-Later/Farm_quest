package nutc.sot.farm_quest.service.auth;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.auth.EmailVerificationRequest;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.repository.EmailVerificationRepository;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceTest {

    private final EmailVerificationRepository emailVerificationRepository = mock(EmailVerificationRepository.class);
    private final GameRepository gameRepository = mock(GameRepository.class);
    private final VisitorAuthService visitorAuthService = mock(VisitorAuthService.class);
    private final RateLimitService rateLimitService = mock(RateLimitService.class);
    private final AuthSecurityService authSecurityService = mock(AuthSecurityService.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final MailService mailService = mock(MailService.class);
    private final AuthProperties authProperties = new AuthProperties();
    private final EmailVerificationService emailVerificationService = new EmailVerificationService(
            emailVerificationRepository,
            gameRepository,
            visitorAuthService,
            rateLimitService,
            authSecurityService,
            sessionService,
            mailService,
            authProperties
    );

    @Test
    void createVerificationRecordsCooldownAfterSuccessfulMailSend() {
        authProperties.setGameCode("farm-quest-mvp");
        authProperties.setOtpExpiryMinutes(10);
        authProperties.setOtpResendCooldownSeconds(30);
        authProperties.setEmailVerificationSecret("secret");

        when(authSecurityService.normalizeEmail("visitor@example.com")).thenReturn("visitor@example.com");
        when(authSecurityService.generateOtp()).thenReturn("123456");
        when(authSecurityService.hashOtp("visitor@example.com", "123456", "secret")).thenReturn("otp-hash");
        when(emailVerificationRepository.findByEmailNormalizedAndStatus("visitor@example.com", "PENDING")).thenReturn(Collections.emptyList());
        when(emailVerificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GameEntity game = new GameEntity();
        game.setCode("farm-quest-mvp");
        when(gameRepository.findByCode("farm-quest-mvp")).thenReturn(Optional.of(game));

        VisitorAccountEntity visitorAccount = new VisitorAccountEntity();
        visitorAccount.setEmailNormalized("visitor@example.com");
        when(visitorAuthService.getOrCreateVisitor("visitor@example.com")).thenReturn(visitorAccount);

        var response = emailVerificationService.createVerification(
                new EmailVerificationRequest("visitor@example.com"),
                "127.0.0.1",
                "JUnit"
        );

        assertThat(response.email()).isEqualTo("visitor@example.com");
        assertThat(response.status()).isEqualTo("PENDING");

        InOrder inOrder = inOrder(emailVerificationRepository, mailService, rateLimitService);
        inOrder.verify(emailVerificationRepository).save(any());
        inOrder.verify(mailService).sendOtp("visitor@example.com", "123456");
        inOrder.verify(rateLimitService).recordVerificationRequested(eq("visitor@example.com"), any(OffsetDateTime.class));
    }

    @Test
    void createVerificationDoesNotRecordCooldownWhenMailSendFails() {
        authProperties.setGameCode("farm-quest-mvp");
        authProperties.setOtpExpiryMinutes(10);
        authProperties.setOtpResendCooldownSeconds(30);
        authProperties.setEmailVerificationSecret("secret");

        when(authSecurityService.normalizeEmail("visitor@example.com")).thenReturn("visitor@example.com");
        when(authSecurityService.generateOtp()).thenReturn("123456");
        when(authSecurityService.hashOtp("visitor@example.com", "123456", "secret")).thenReturn("otp-hash");
        when(emailVerificationRepository.findByEmailNormalizedAndStatus("visitor@example.com", "PENDING")).thenReturn(Collections.emptyList());
        when(emailVerificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GameEntity game = new GameEntity();
        game.setCode("farm-quest-mvp");
        when(gameRepository.findByCode("farm-quest-mvp")).thenReturn(Optional.of(game));

        VisitorAccountEntity visitorAccount = new VisitorAccountEntity();
        visitorAccount.setEmailNormalized("visitor@example.com");
        when(visitorAuthService.getOrCreateVisitor("visitor@example.com")).thenReturn(visitorAccount);
        doThrow(new AuthException(AuthErrorCode.EMAIL_DELIVERY_FAILED, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP email"))
                .when(mailService).sendOtp("visitor@example.com", "123456");

        assertThatThrownBy(() -> emailVerificationService.createVerification(
                new EmailVerificationRequest("visitor@example.com"),
                "127.0.0.1",
                "JUnit"
        ))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthException authException = (AuthException) exception;
                    assertThat(authException.getErrorCode()).isEqualTo(AuthErrorCode.EMAIL_DELIVERY_FAILED);
                });

        verify(rateLimitService, never()).recordVerificationRequested(eq("visitor@example.com"), any(OffsetDateTime.class));
    }
}
