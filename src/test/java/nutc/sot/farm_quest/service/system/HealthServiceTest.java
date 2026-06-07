package nutc.sot.farm_quest.service.system;

import java.time.OffsetDateTime;
import java.util.List;
import nutc.sot.farm_quest.dto.system.DependencyItemResponse;
import nutc.sot.farm_quest.dto.system.DependencyStatusResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthServiceTest {

    private final DependencyCheckService dependencyCheckService = mock(DependencyCheckService.class);
    private final HealthService healthService = new HealthService(dependencyCheckService);

    @Test
    void returnsUpWhenDependenciesAreHealthy() {
        ReflectionTestUtils.setField(healthService, "applicationName", "Farm quest");
        when(dependencyCheckService.checkDependencies()).thenReturn(new DependencyStatusResponse(
                "UP",
                OffsetDateTime.now(),
                List.of(new DependencyItemResponse("PostgreSQL", "UP", "Connection successful"))
        ));

        var response = healthService.getHealth();

        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.application()).isEqualTo("Farm quest");
        assertThat(response.seedDataReady()).isTrue();
    }

    @Test
    void returnsDegradedWhenDependenciesAreDown() {
        ReflectionTestUtils.setField(healthService, "applicationName", "Farm quest");
        when(dependencyCheckService.checkDependencies()).thenReturn(new DependencyStatusResponse(
                "DOWN",
                OffsetDateTime.now(),
                List.of(new DependencyItemResponse("PostgreSQL", "DOWN", "Dependency check failed"))
        ));

        var response = healthService.getHealth();

        assertThat(response.status()).isEqualTo("DEGRADED");
        assertThat(response.seedDataReady()).isFalse();
    }
}
