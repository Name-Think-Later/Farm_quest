package nutc.sot.farm_quest.persistence.repository;

import java.util.List;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AdminStatisticsRepository extends Repository<QuestProgressEntity, UUID> {

    @Query(value = """
            select
                cast((select count(*) from quest_progress qp where qp.game_id = :gameId and qp.status = 'COMPLETED') as bigint) as completedQuestCount,
                cast((select count(*) from coupon c where c.game_id = :gameId) as bigint) as issuedCouponCount,
                cast((select count(*)
                      from coupon_usage cu
                      join coupon c on c.id = cu.coupon_id
                     where c.game_id = :gameId) as bigint) as usedCouponCount,
                case
                    when (select count(*) from coupon c where c.game_id = :gameId) = 0 then 0
                    else (
                        (select count(*)
                           from coupon_usage cu
                           join coupon c on c.id = cu.coupon_id
                          where c.game_id = :gameId)::double precision
                        / (select count(*) from coupon c where c.game_id = :gameId)
                    )
                end as usageRate
            """, nativeQuery = true)
    OverviewStatisticsProjection summarizeByGameId(@Param("gameId") UUID gameId);

    @Query(value = """
            select
                q.id as questId,
                q.code as questCode,
                q.title as questTitle,
                cast(count(qp.id) as bigint) as completedQuestCount
            from quest q
            left join quest_progress qp
              on qp.quest_id = q.id
             and qp.status = 'COMPLETED'
            where q.game_id = :gameId
            group by q.id, q.code, q.title, q.sort_order
            order by q.sort_order asc
            """, nativeQuery = true)
    List<QuestCompletionStatisticsProjection> summarizeQuestCompletionByGameId(@Param("gameId") UUID gameId);

    @Query(value = """
            select
                cc.id as campaignId,
                cc.quest_id as questId,
                cc.code as campaignCode,
                cc.title as campaignTitle,
                cast(count(distinct c.id) as bigint) as issuedCouponCount,
                cast(count(distinct cu.id) as bigint) as usedCouponCount,
                case
                    when count(distinct c.id) = 0 then 0
                    else count(distinct cu.id)::double precision / count(distinct c.id)
                end as usageRate
            from coupon_campaign cc
            left join coupon c on c.coupon_campaign_id = cc.id
            left join coupon_usage cu on cu.coupon_id = c.id
            where cc.game_id = :gameId
            group by cc.id, cc.quest_id, cc.code, cc.title, cc.updated_at
            order by cc.updated_at desc
            """, nativeQuery = true)
    List<CouponCampaignStatisticsProjection> summarizeCouponCampaignByGameId(@Param("gameId") UUID gameId);

    interface OverviewStatisticsProjection {
        long getCompletedQuestCount();
        long getIssuedCouponCount();
        long getUsedCouponCount();
        double getUsageRate();
    }

    interface QuestCompletionStatisticsProjection {
        UUID getQuestId();
        String getQuestCode();
        String getQuestTitle();
        long getCompletedQuestCount();
    }

    interface CouponCampaignStatisticsProjection {
        UUID getCampaignId();
        UUID getQuestId();
        String getCampaignCode();
        String getCampaignTitle();
        long getIssuedCouponCount();
        long getUsedCouponCount();
        double getUsageRate();
    }
}
