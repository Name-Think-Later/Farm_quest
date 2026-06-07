package nutc.sot.farm_quest.persistence.repository;

import nutc.sot.farm_quest.persistence.entity.EmailVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, UUID> {
    Optional<EmailVerificationEntity> findFirstByEmailNormalizedAndStatusAndExpiresAtAfterOrderByExpiresAtDesc(String emailNormalized, String status, OffsetDateTime now);
}
