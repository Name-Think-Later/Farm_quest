package nutc.sot.farm_quest.controller.game;

import java.time.OffsetDateTime;
import java.util.UUID;
import nutc.sot.farm_quest.dto.game.GameEntryResponse;
import nutc.sot.farm_quest.dto.game.GameStateResponse;
import nutc.sot.farm_quest.service.game.GameService;
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

class GameControllerTest {

    private final GameService gameService = mock(GameService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new GameController(gameService))
            .build();

    @Test
    void getGameReturnsContract() throws Exception {
        when(gameService.getActiveGame()).thenReturn(new GameEntryResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "farm-quest-mvp",
                "Farm Quest MVP",
                "/play",
                OffsetDateTime.parse("2026-06-07T10:15:30+08:00"),
                null
        ));

        mockMvc.perform(get("/api/game"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.gameId").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.code").value("farm-quest-mvp"));
    }

    @Test
    void getGameStateReturnsContract() throws Exception {
        when(gameService.getGameState("session-token")).thenReturn(new GameStateResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "茶園謎題",
                "STARTED",
                false,
                false,
                "VERIFY_LOCATION"
        ));

        mockMvc.perform(get("/api/game/state")
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.visitorAccountId").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(jsonPath("$.currentQuestTitle").value("茶園謎題"))
                .andExpect(jsonPath("$.progressStatus").value("STARTED"));
    }

    @Test
    void getGameStatePreservesIndependentAiRiddleAvailability() throws Exception {
        when(gameService.getGameState("session-token")).thenReturn(new GameStateResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "茶園謎題",
                "LOCATION_VERIFIED",
                true,
                false,
                "AI_RIDDLE_AVAILABLE"
        ));

        mockMvc.perform(get("/api/game/state")
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gpsVerified").value(true))
                .andExpect(jsonPath("$.aiRiddleAvailable").value(false));
    }
}
