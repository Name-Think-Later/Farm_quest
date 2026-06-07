package nutc.sot.farm_quest.persistence.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.EmailVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, UUID> {
    Optional<EmailVerificationEntity> findFirstByEmailNormalizedAndStatusOrderByRequestedAtDesc(String emailNormalized, String status);
    List<EmailVerificationEntity> findByEmailNormalizedAndStatus(String emailNormalized, String status);
    long countByEmailNormalizedAndRequestedAtAfter(String emailNormalized, OffsetDateTime requestedAt);
}
