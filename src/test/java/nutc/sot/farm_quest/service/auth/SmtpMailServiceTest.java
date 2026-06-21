package nutc.sot.farm_quest.service.auth;

import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpMailServiceTest {

    private final JavaMailSender javaMailSender = mock(JavaMailSender.class);
    private final AuthProperties authProperties = new AuthProperties();
    private final SmtpMailService smtpMailService = new SmtpMailService(javaMailSender, authProperties);

    @Test
    void sendOtpBuildsAndSendsMessage() {
        authProperties.setMailFrom("no-reply@example.com");
        authProperties.setMailSubject("Farm Quest 驗證碼");
        authProperties.setOtpExpiryMinutes(10);

        smtpMailService.sendOtp("visitor@example.com", "123456");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getFrom()).isEqualTo("no-reply@example.com");
        assertThat(message.getTo()).containsExactly("visitor@example.com");
        assertThat(message.getSubject()).isEqualTo("Farm Quest 驗證碼");
        assertThat(message.getText()).contains("123456");
        assertThat(message.getText()).contains("10 分鐘");
    }

    @Test
    void sendOtpThrowsAuthExceptionWhenMailDeliveryFails() {
        authProperties.setMailFrom("no-reply@example.com");
        authProperties.setMailSubject("Farm Quest 驗證碼");
        authProperties.setOtpExpiryMinutes(10);
        doThrow(new MailSendException("smtp failed")).when(javaMailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

        assertThatThrownBy(() -> smtpMailService.sendOtp("visitor@example.com", "123456"))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthException authException = (AuthException) exception;
                    assertThat(authException.getErrorCode()).isEqualTo(AuthErrorCode.EMAIL_DELIVERY_FAILED);
                })
                .hasMessage("Failed to send OTP email");
    }
}
