package nutc.sot.farm_quest.service.admin;

import nutc.sot.farm_quest.config.AdminAuthProperties;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminAuthServiceTest {

    @Test
    void requireAdminAcceptsMatchingSecret() {
        AdminAuthProperties properties = new AdminAuthProperties();
        properties.setAuthSecret("admin-secret");
        AdminAuthService service = new AdminAuthService(properties);

        assertThatCode(() -> service.requireAdmin("admin-secret"))
                .doesNotThrowAnyException();
    }

    @Test
    void requireAdminRejectsMissingSecretConfiguration() {
        AdminAuthProperties properties = new AdminAuthProperties();
        AdminAuthService service = new AdminAuthService(properties);

        assertThatThrownBy(() -> service.requireAdmin("admin-secret"))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthException authException = (AuthException) exception;
                    org.assertj.core.api.Assertions.assertThat(authException.getErrorCode()).isEqualTo(AuthErrorCode.ADMIN_SECRET_NOT_CONFIGURED);
                });
    }

    @Test
    void requireAdminRejectsInvalidToken() {
        AdminAuthProperties properties = new AdminAuthProperties();
        properties.setAuthSecret("admin-secret");
        AdminAuthService service = new AdminAuthService(properties);

        assertThatThrownBy(() -> service.requireAdmin("visitor-session-token"))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthException authException = (AuthException) exception;
                    org.assertj.core.api.Assertions.assertThat(authException.getErrorCode()).isEqualTo(AuthErrorCode.ADMIN_UNAUTHORIZED);
                });
    }
}
