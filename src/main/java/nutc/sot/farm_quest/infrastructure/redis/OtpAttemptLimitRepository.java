package nutc.sot.farm_quest.infrastructure.redis;

import org.springframework.data.repository.CrudRepository;

public interface OtpAttemptLimitRepository extends CrudRepository<OtpAttemptLimit, String> {
}
