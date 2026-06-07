package nutc.sot.farm_quest.persistence.repository;

import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VisitorSessionRepository extends JpaRepository<VisitorSessionEntity, UUID> {
    Optional<VisitorSessionEntity> findFirstByTokenHashAndStatusAndRevokedAtIsNullAndExpiresAtAfterOrderByExpiresAtDesc(String tokenHash, String status, OffsetDateTime now);
    List<VisitorSessionEntity> findByVisitorAccountIdAndStatusOrderByExpiresAtDesc(UUID visitorAccountId, String status);
}
