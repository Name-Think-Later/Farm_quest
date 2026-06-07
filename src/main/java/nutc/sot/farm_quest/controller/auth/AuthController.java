package nutc.sot.farm_quest.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.auth.ConfirmEmailVerificationRequest;
import nutc.sot.farm_quest.dto.auth.CurrentUserResponse;
import nutc.sot.farm_quest.dto.auth.EmailVerificationRequest;
import nutc.sot.farm_quest.dto.auth.EmailVerificationResponse;
import nutc.sot.farm_quest.dto.auth.LogoutResponse;
import nutc.sot.farm_quest.dto.auth.VisitorSessionResponse;
import nutc.sot.farm_quest.service.auth.EmailVerificationService;
import nutc.sot.farm_quest.service.auth.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final SessionService sessionService;

    @PostMapping("/visitor/email-verifications")
    public ResponseEntity<EmailVerificationResponse> createEmailVerification(@Valid @RequestBody EmailVerificationRequest request,
                                                                             HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(emailVerificationService.createVerification(
                request,
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    @PostMapping("/visitor/email-verifications/confirm")
    public ResponseEntity<VisitorSessionResponse> confirmEmailVerification(@Valid @RequestBody ConfirmEmailVerificationRequest request,
                                                                           HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(emailVerificationService.confirmVerification(
                request,
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    @GetMapping("/visitor/session")
    public ResponseEntity<VisitorSessionResponse> getCurrentSession(HttpServletRequest httpServletRequest) {
        var session = sessionService.requireActiveSession(extractBearerToken(httpServletRequest));
        return ResponseEntity.ok(new VisitorSessionResponse(
                session.getVisitorAccount().getId(),
                session.getVisitorAccount().getEmailNormalized(),
                null,
                session.getIssuedAt(),
                session.getExpiresAt(),
                true
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(sessionService.getCurrentUser(extractBearerToken(httpServletRequest)));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(sessionService.logout(extractBearerToken(httpServletRequest)));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
