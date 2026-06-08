package nutc.sot.farm_quest.persistence.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorSessionRepository extends JpaRepository<VisitorSessionEntity, UUID> {
    Optional<VisitorSessionEntity> findByTokenHash(String tokenHash);
    Optional<VisitorSessionEntity> findFirstByTokenHashAndStatusAndRevokedAtIsNullAndExpiresAtAfterOrderByExpiresAtDesc(String tokenHash, String status, OffsetDateTime now);
}
