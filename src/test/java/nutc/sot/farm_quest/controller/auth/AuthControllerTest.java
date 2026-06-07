package nutc.sot.farm_quest.controller.auth;

import nutc.sot.farm_quest.dto.auth.ConfirmEmailVerificationRequest;
import nutc.sot.farm_quest.dto.auth.CurrentUserResponse;
import nutc.sot.farm_quest.dto.auth.EmailVerificationRequest;
import nutc.sot.farm_quest.dto.auth.EmailVerificationResponse;
import nutc.sot.farm_quest.dto.auth.LogoutResponse;
import nutc.sot.farm_quest.dto.auth.VisitorSessionResponse;
import java.time.OffsetDateTime;
import java.util.UUID;
import nutc.sot.farm_quest.exception.GlobalExceptionHandler;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.service.auth.EmailVerificationService;
import nutc.sot.farm_quest.service.auth.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private final EmailVerificationService emailVerificationService = mock(EmailVerificationService.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(emailVerificationService, sessionService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void createEmailVerificationReturnsContract() throws Exception {
        when(emailVerificationService.createVerification(any(EmailVerificationRequest.class), nullable(String.class), nullable(String.class)))
                .thenReturn(new EmailVerificationResponse(
                        "visitor@example.com",
                        OffsetDateTime.parse("2026-06-07T10:10:00+08:00"),
                        OffsetDateTime.parse("2026-06-07T10:01:00+08:00"),
                        "PENDING"
                ));

        mockMvc.perform(post("/api/auth/visitor/email-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"visitor@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("visitor@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void confirmEmailVerificationReturnsSessionToken() throws Exception {
        when(emailVerificationService.confirmVerification(any(ConfirmEmailVerificationRequest.class), nullable(String.class), nullable(String.class)))
                .thenReturn(new VisitorSessionResponse(
                        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                        "visitor@example.com",
                        "session-token",
                        OffsetDateTime.parse("2026-06-07T10:00:00+08:00"),
                        OffsetDateTime.parse("2026-06-08T10:00:00+08:00"),
                        true
                ));

        mockMvc.perform(post("/api/auth/visitor/email-verifications/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"visitor@example.com","otp":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionToken").value("session-token"))
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    void getCurrentSessionReturnsContract() throws Exception {
        VisitorAccountEntity visitorAccount = new VisitorAccountEntity();
        visitorAccount.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        visitorAccount.setEmailNormalized("visitor@example.com");

        VisitorSessionEntity visitorSession = new VisitorSessionEntity();
        visitorSession.setVisitorAccount(visitorAccount);
        visitorSession.setIssuedAt(OffsetDateTime.parse("2026-06-07T10:00:00+08:00"));
        visitorSession.setExpiresAt(OffsetDateTime.parse("2026-06-08T10:00:00+08:00"));

        when(sessionService.requireActiveSession("session-token")).thenReturn(visitorSession);

        mockMvc.perform(get("/api/auth/visitor/session")
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitorAccountId").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(jsonPath("$.email").value("visitor@example.com"))
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    void getCurrentUserReturnsContract() throws Exception {
        when(sessionService.getCurrentUser("session-token"))
                .thenReturn(new CurrentUserResponse(
                        true,
                        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                        "visitor@example.com",
                        OffsetDateTime.parse("2026-06-08T10:00:00+08:00")
                ));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.email").value("visitor@example.com"));
    }

    @Test
    void logoutReturnsSuccess() throws Exception {
        when(sessionService.logout("session-token")).thenReturn(new LogoutResponse(true));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
