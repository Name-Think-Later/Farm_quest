package nutc.sot.farm_quest.service.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthSecurityServiceTest {

    private final AuthSecurityService authSecurityService = new AuthSecurityService();

    @Test
    void normalizeEmailTrimsAndLowercases() {
        assertEquals("visitor@example.com", authSecurityService.normalizeEmail("  Visitor@Example.com  "));
    }

    @Test
    void generateOtpReturnsSixDigits() {
        String otp = authSecurityService.generateOtp();
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void hashSessionTokenIsDeterministic() {
        String hash1 = authSecurityService.hashSessionToken("token", "secret");
        String hash2 = authSecurityService.hashSessionToken("token", "secret");
        String hash3 = authSecurityService.hashSessionToken("token-2", "secret");

        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
    }
}
