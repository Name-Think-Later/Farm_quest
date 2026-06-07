package nutc.sot.farm_quest.persistence.repository;

import nutc.sot.farm_quest.persistence.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<LocationEntity, UUID> {
    List<LocationEntity> findByPrimaryTrue();
}
