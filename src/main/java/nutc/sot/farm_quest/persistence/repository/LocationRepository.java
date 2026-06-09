package nutc.sot.farm_quest.persistence.repository;

import java.util.List;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<LocationEntity, UUID> {
    List<LocationEntity> findByPrimaryTrue();
    List<LocationEntity> findByQuest_IdOrderBySortOrderAsc(UUID questId);
    List<LocationEntity> findByQuest_IdAndStatusOrderBySortOrderAsc(UUID questId, String status);

    @Query("""
            select location
            from LocationEntity location
            where location.quest.game.id = :gameId
            order by location.quest.sortOrder asc, location.sortOrder asc
            """)
    List<LocationEntity> findByGameIdOrderByQuestSortOrderAscLocationSortOrderAsc(@Param("gameId") UUID gameId);

    @Query("""
            select location
            from LocationEntity location
            where location.quest.game.id = :gameId
              and location.status = :status
            order by location.quest.sortOrder asc, location.sortOrder asc
            """)
    List<LocationEntity> findByGameIdAndStatusOrderByQuestSortOrderAscLocationSortOrderAsc(@Param("gameId") UUID gameId,
                                                                                            @Param("status") String status);
}
