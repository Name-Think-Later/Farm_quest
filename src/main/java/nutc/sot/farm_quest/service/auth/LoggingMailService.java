package nutc.sot.farm_quest.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nutc.sot.farm_quest.config.AuthProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoggingMailService implements MailService {

    private final AuthProperties authProperties;

    @Override
    public void sendOtp(String email, String otp) {
        log.info("OTP email stub from={} to={} otp={}", authProperties.getMailFrom(), email, otp);
    }
}
