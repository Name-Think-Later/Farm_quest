package nutc.sot.farm_quest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "farm-quest.auth")
public class AuthProperties {

    private String gameCode;
    private int otpExpiryMinutes;
    private int sessionHours;
    private int otpResendCooldownSeconds;
    private int otpMaxRequestsPerHour;
    private int otpMaxAttempts;
    private String sessionSecret;
    private String emailVerificationSecret;
    private String mailMode;
    private String mailFrom;
    private String mailSubject;
    private String mailHost;
    private Integer mailPort;
    private String mailUsername;
    private String mailPassword;
    private boolean mailAuth;
    private boolean mailStarttlsEnabled;
    private boolean mailSslEnabled;
}
