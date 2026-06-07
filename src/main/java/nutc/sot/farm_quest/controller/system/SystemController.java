package nutc.sot.farm_quest.controller.system;

import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.system.DependencyStatusResponse;
import nutc.sot.farm_quest.dto.system.HealthResponse;
import nutc.sot.farm_quest.service.system.DependencyCheckService;
import nutc.sot.farm_quest.service.system.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final HealthService healthService;
    private final DependencyCheckService dependencyCheckService;

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        return ResponseEntity.ok(healthService.getHealth());
    }

    @GetMapping("/dependencies")
    public ResponseEntity<DependencyStatusResponse> getDependencies() {
        return ResponseEntity.ok(dependencyCheckService.checkDependencies());
    }
}
