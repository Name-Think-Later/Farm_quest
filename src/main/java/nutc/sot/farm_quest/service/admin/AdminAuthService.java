package nutc.sot.farm_quest.service.admin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AdminAuthProperties;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminAuthProperties adminAuthProperties;

    public void requireAdmin(String token) {
        String configuredSecret = adminAuthProperties.getAuthSecret();
        if (!StringUtils.hasText(configuredSecret)) {
            throw new AuthException(AuthErrorCode.ADMIN_SECRET_NOT_CONFIGURED, HttpStatus.SERVICE_UNAVAILABLE, "Admin secret is not configured");
        }
        if (!StringUtils.hasText(token) || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), configuredSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new AuthException(AuthErrorCode.ADMIN_UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Admin authorization failed");
        }
    }
}
