package nutc.sot.farm_quest.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.VisitorAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorAccountRepository extends JpaRepository<VisitorAccountEntity, UUID> {
    Optional<VisitorAccountEntity> findByEmailNormalized(String emailNormalized);
}
