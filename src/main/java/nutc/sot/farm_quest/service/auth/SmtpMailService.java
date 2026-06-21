package nutc.sot.farm_quest.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "farm-quest.auth", name = "mail-mode", havingValue = "smtp")
public class SmtpMailService implements MailService {

    private final JavaMailSender javaMailSender;
    private final AuthProperties authProperties;

    @Override
    public void sendOtp(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(authProperties.getMailFrom());
            message.setTo(email);
            message.setSubject(authProperties.getMailSubject());
            message.setText(buildMessageBody(otp));
            javaMailSender.send(message);
            log.info("OTP email sent from={} to={} mode=smtp", authProperties.getMailFrom(), email);
        } catch (MailException exception) {
            log.error("Failed to send OTP email from={} to={} mode=smtp", authProperties.getMailFrom(), email, exception);
            throw new AuthException(
                    AuthErrorCode.EMAIL_DELIVERY_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send OTP email"
            );
        }
    }

    private String buildMessageBody(String otp) {
        return "Farm Quest 驗證碼：" + otp + System.lineSeparator()
                + System.lineSeparator()
                + "此驗證碼將在 " + authProperties.getOtpExpiryMinutes() + " 分鐘後失效。" + System.lineSeparator()
                + "若這不是你的操作，請忽略此信件。";
    }
}
