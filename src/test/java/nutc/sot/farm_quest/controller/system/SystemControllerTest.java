package nutc.sot.farm_quest.controller.system;

import java.time.OffsetDateTime;
import java.util.List;
import nutc.sot.farm_quest.dto.system.DependencyItemResponse;
import nutc.sot.farm_quest.dto.system.DependencyStatusResponse;
import nutc.sot.farm_quest.dto.system.HealthResponse;
import nutc.sot.farm_quest.service.system.DependencyCheckService;
import nutc.sot.farm_quest.service.system.HealthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SystemControllerTest {

    private final HealthService healthService = mock(HealthService.class);
    private final DependencyCheckService dependencyCheckService = mock(DependencyCheckService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SystemController(healthService, dependencyCheckService))
            .build();

    @Test
    void getHealthReturnsContract() throws Exception {
        when(healthService.getHealth()).thenReturn(new HealthResponse(
                "UP",
                "Farm quest",
                OffsetDateTime.parse("2026-06-07T10:15:30+08:00"),
                true
        ));

        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("Farm quest"))
                .andExpect(jsonPath("$.seedDataReady").value(true));
    }

    @Test
    void getDependenciesReturnsContract() throws Exception {
        when(dependencyCheckService.checkDependencies()).thenReturn(new DependencyStatusResponse(
                "DOWN",
                OffsetDateTime.parse("2026-06-07T10:15:30+08:00"),
                List.of(
                        new DependencyItemResponse("PostgreSQL", "UP", "Connection successful"),
                        new DependencyItemResponse("Redis", "DOWN", "Connection refused"),
                        new DependencyItemResponse("Qdrant", "UP", "Collection reachable"),
                        new DependencyItemResponse("Spring AI Provider", "MISSING", "Provider configuration incomplete")
                )
        ));

        mockMvc.perform(get("/api/system/dependencies"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.dependencies.length()").value(4))
                .andExpect(jsonPath("$.dependencies[1].name").value("Redis"))
                .andExpect(jsonPath("$.dependencies[1].status").value("DOWN"));
    }
}
