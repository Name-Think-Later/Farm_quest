package nutc.sot.farm_quest.service.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import nutc.sot.farm_quest.config.AuthProperties;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class LoggingMailServiceTest {

    @Test
    void sendOtpDoesNotLogOtpValue(CapturedOutput output) {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setMailFrom("no-reply@example.com");
        authProperties.setMailMode("log");

        LoggingMailService loggingMailService = new LoggingMailService(authProperties);
        loggingMailService.sendOtp("visitor@example.com", "123456");

        assertThat(output.getOut()).contains("OTP email stub triggered");
        assertThat(output.getOut()).contains("visitor@example.com");
        assertThat(output.getOut()).doesNotContain("123456");
    }
}
