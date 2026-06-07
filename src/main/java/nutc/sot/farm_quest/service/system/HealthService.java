package nutc.sot.farm_quest.service.system;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.system.DependencyStatusResponse;
import nutc.sot.farm_quest.dto.system.HealthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final DependencyCheckService dependencyCheckService;

    @Value("${spring.application.name}")
    private String applicationName;

    public HealthResponse getHealth() {
        DependencyStatusResponse dependencyStatus = dependencyCheckService.checkDependencies();
        boolean healthy = "UP".equals(dependencyStatus.status());

        return new HealthResponse(
                healthy ? "UP" : "DEGRADED",
                applicationName,
                OffsetDateTime.now(),
                healthy
        );
    }
}
