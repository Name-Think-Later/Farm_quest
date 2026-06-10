package nutc.sot.farm_quest.controller.game;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.common.ApiResponse;
import nutc.sot.farm_quest.dto.game.GameEntryResponse;
import nutc.sot.farm_quest.dto.game.GameStateResponse;
import nutc.sot.farm_quest.service.game.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<ApiResponse<GameEntryResponse>> getGame() {
        return ResponseEntity.ok(ApiResponse.success(gameService.getActiveGame()));
    }

    @GetMapping("/state")
    public ResponseEntity<ApiResponse<GameStateResponse>> getGameState(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(gameService.getGameState(extractBearerToken(request))));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
