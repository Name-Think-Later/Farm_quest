package nutc.sot.farm_quest.persistence.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.EmailVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, UUID> {
    Optional<EmailVerificationEntity> findFirstByEmailNormalizedAndStatusOrderByRequestedAtDesc(String emailNormalized, String status);
    Optional<EmailVerificationEntity> findFirstByEmailNormalizedAndStatusAndExpiresAtAfterOrderByRequestedAtDesc(String emailNormalized, String status, OffsetDateTime now);
    List<EmailVerificationEntity> findByEmailNormalizedAndStatus(String emailNormalized, String status);
    long countByEmailNormalizedAndRequestedAtAfter(String emailNormalized, OffsetDateTime requestedAt);

    @Modifying
    @Query("""
            update EmailVerificationEntity verification
               set verification.status = :expiredStatus
             where verification.status = :pendingStatus
               and verification.expiresAt <= :time
            """)
    int expirePendingVerifications(@Param("pendingStatus") String pendingStatus,
                                   @Param("expiredStatus") String expiredStatus,
                                   @Param("time") OffsetDateTime time);

    @Modifying
    @Query("""
            delete from EmailVerificationEntity verification
             where verification.status in :statuses
               and verification.expiresAt < :cutoff
            """)
    int deleteExpiredHistory(@Param("statuses") Collection<String> statuses,
                             @Param("cutoff") OffsetDateTime cutoff);
}
