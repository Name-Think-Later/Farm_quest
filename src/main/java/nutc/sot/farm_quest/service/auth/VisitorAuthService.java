package nutc.sot.farm_quest.service.auth;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.config.AuthProperties;
import nutc.sot.farm_quest.dto.auth.CurrentUserResponse;
import nutc.sot.farm_quest.persistence.entity.GameEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import nutc.sot.farm_quest.persistence.repository.GameRepository;
import nutc.sot.farm_quest.persistence.repository.VisitorAccountRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VisitorAuthService {

    private final VisitorAccountRepository visitorAccountRepository;
    private final GameRepository gameRepository;
    private final AuthProperties authProperties;
    private final AuthSecurityService authSecurityService;

    public VisitorAccountEntity getOrCreateVisitor(String normalizedEmail) {
        return visitorAccountRepository.findByEmailNormalized(normalizedEmail)
                .orElseGet(() -> createVisitor(normalizedEmail));
    }

    public VisitorAccountEntity activateVisitor(VisitorAccountEntity visitorAccount) {
        OffsetDateTime now = OffsetDateTime.now();
        visitorAccount.setStatus("ACTIVE");
        visitorAccount.setEmailVerifiedAt(now);
        visitorAccount.setLastLoginAt(now);
        visitorAccount.setUpdatedAt(now);
        return visitorAccountRepository.save(visitorAccount);
    }

    public CurrentUserResponse toCurrentUserResponse(VisitorAccountEntity visitorAccount, OffsetDateTime sessionExpiresAt) {
        return new CurrentUserResponse(
                true,
                visitorAccount.getId(),
                visitorAccount.getEmailNormalized(),
                sessionExpiresAt
        );
    }

    private VisitorAccountEntity createVisitor(String normalizedEmail) {
        GameEntity game = gameRepository.findByCode(authProperties.getGameCode())
                .orElseThrow(() -> new IllegalStateException("Configured game not found"));
        OffsetDateTime now = OffsetDateTime.now();
        VisitorAccountEntity visitorAccount = new VisitorAccountEntity();
        visitorAccount.setId(UUID.randomUUID());
        visitorAccount.setGame(game);
        visitorAccount.setEmailNormalized(normalizedEmail);
        visitorAccount.setEmailHash(authSecurityService.hashEmail(normalizedEmail));
        visitorAccount.setStatus("PENDING_VERIFICATION");
        visitorAccount.setCreatedAt(now);
        visitorAccount.setUpdatedAt(now);
        return visitorAccountRepository.save(visitorAccount);
    }
}
