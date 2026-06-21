package nutc.sot.farm_quest.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nutc.sot.farm_quest.config.AuthProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "farm-quest.auth", name = "mail-mode", havingValue = "log", matchIfMissing = true)
public class LoggingMailService implements MailService {

    private final AuthProperties authProperties;

    @Override
    public void sendOtp(String email, String otp) {
        log.info("OTP email stub triggered from={} to={} mode={}", authProperties.getMailFrom(), email, authProperties.getMailMode());
    }
}
