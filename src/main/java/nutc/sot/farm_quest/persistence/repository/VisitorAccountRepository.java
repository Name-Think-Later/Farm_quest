package nutc.sot.farm_quest.persistence.repository;

import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VisitorAccountRepository extends JpaRepository<VisitorAccountEntity, UUID> {
    Optional<VisitorAccountEntity> findByEmailNormalized(String emailNormalized);
}
