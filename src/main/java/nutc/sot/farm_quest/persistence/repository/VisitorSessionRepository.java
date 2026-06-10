package nutc.sot.farm_quest.persistence.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitorSessionRepository extends JpaRepository<VisitorSessionEntity, UUID> {
    Optional<VisitorSessionEntity> findByTokenHash(String tokenHash);
    Optional<VisitorSessionEntity> findFirstByTokenHashAndStatusAndRevokedAtIsNullAndExpiresAtAfterOrderByExpiresAtDesc(String tokenHash, String status, OffsetDateTime now);

    @Modifying
    @Query("""
            update VisitorSessionEntity session
               set session.status = :expiredStatus,
                   session.updatedAt = :time
             where session.status = :activeStatus
               and session.expiresAt <= :time
               and session.revokedAt is null
            """)
    int expireActiveSessions(@Param("activeStatus") String activeStatus,
                             @Param("expiredStatus") String expiredStatus,
                             @Param("time") OffsetDateTime time);

    @Modifying
    @Query("""
            delete from VisitorSessionEntity session
             where session.status in :statuses
               and session.expiresAt < :cutoff
            """)
    int deleteHistoricalSessions(@Param("statuses") Collection<String> statuses,
                                 @Param("cutoff") OffsetDateTime cutoff);
}
