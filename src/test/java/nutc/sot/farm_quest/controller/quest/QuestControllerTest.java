package nutc.sot.farm_quest.controller.quest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import nutc.sot.farm_quest.dto.quest.LocationHintResponse;
import nutc.sot.farm_quest.dto.quest.LocationVerificationRequest;
import nutc.sot.farm_quest.dto.quest.LocationVerificationResponse;
import nutc.sot.farm_quest.dto.quest.QuestDetailResponse;
import nutc.sot.farm_quest.dto.quest.QuestListResponse;
import nutc.sot.farm_quest.dto.quest.StartQuestResponse;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import nutc.sot.farm_quest.exception.GlobalExceptionHandler;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.service.quest.QuestService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QuestControllerTest {

    private final QuestService questService = mock(QuestService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new QuestController(questService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void getQuestsReturnsContract() throws Exception {
        when(questService.getVisibleQuests("session-token")).thenReturn(new QuestListResponse(List.of(
                new QuestDetailResponse(
                        UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        "茶園謎題",
                        "作為階段二最小可用資料的示範任務。",
                        1,
                        "STARTED",
                        OffsetDateTime.parse("2026-06-07T10:00:00+08:00"),
                        null,
                        true,
                        "VERIFY_LOCATION"
                )
        )));

        mockMvc.perform(get("/api/quests")
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.quests.length()").value(1))
                .andExpect(jsonPath("$.quests[0].title").value("茶園謎題"))
                .andExpect(jsonPath("$.quests[0].status").value("STARTED"));
    }

    @Test
    void getQuestsRejectsMissingSession() throws Exception {
        when(questService.getVisibleQuests(null))
                .thenThrow(new AuthException(AuthErrorCode.SESSION_INVALID, HttpStatus.UNAUTHORIZED, "Session token is invalid"));

        mockMvc.perform(get("/api/quests"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SESSION_INVALID"));
    }

    @Test
    void getCurrentQuestReturnsContract() throws Exception {
        when(questService.getCurrentQuest("session-token")).thenReturn(new QuestDetailResponse(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "茶園謎題",
                "作為階段二最小可用資料的示範任務。",
                1,
                "STARTED",
                OffsetDateTime.parse("2026-06-07T10:00:00+08:00"),
                null,
                true,
                "VERIFY_LOCATION"
        ));

        mockMvc.perform(get("/api/quests/current")
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.current").value(true))
                .andExpect(jsonPath("$.title").value("茶園謎題"));
    }

    @Test
    void startQuestReturnsContract() throws Exception {
        UUID questId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(questService.startQuest("session-token", questId)).thenReturn(new StartQuestResponse(
                questId,
                "STARTED",
                OffsetDateTime.parse("2026-06-07T10:00:00+08:00"),
                "VERIFY_LOCATION"
        ));

        mockMvc.perform(post("/api/quests/{questId}/start", questId)
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questId").value("22222222-2222-2222-2222-222222222222"))
                .andExpect(jsonPath("$.status").value("STARTED"));
    }

    @Test
    void getLocationHintReturnsContract() throws Exception {
        UUID questId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(questService.getLocationHint("session-token", questId)).thenReturn(new LocationHintResponse(
                questId,
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "主要茶園定位點",
                "請前往茶園入口附近。",
                30,
                50
        ));

        mockMvc.perform(get("/api/quests/{questId}/location-hint", questId)
                        .header("Authorization", "Bearer session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationName").value("主要茶園定位點"))
                .andExpect(jsonPath("$.maxAccuracyMeters").value(50));
    }

    @Test
    void verifyLocationReturnsContract() throws Exception {
        UUID questId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(questService.verifyLocation(eq("session-token"), eq(questId), any(LocationVerificationRequest.class)))
                .thenReturn(new LocationVerificationResponse(
                        questId,
                        "LOCATION_VERIFIED",
                        true,
                        5.2,
                        10.0,
                        OffsetDateTime.parse("2026-06-07T10:10:00+08:00"),
                        "AI_RIDDLE_AVAILABLE"
                ));

        mockMvc.perform(post("/api/quests/{questId}/location-verifications", questId)
                        .header("Authorization", "Bearer session-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"permissionDenied":false,"latitude":24.147736,"longitude":120.673648,"accuracyMeters":10.0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.status").value("LOCATION_VERIFIED"));
    }

    @Test
    void verifyLocationReturnsPermissionError() throws Exception {
        UUID questId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(questService.verifyLocation(eq("session-token"), eq(questId), any(LocationVerificationRequest.class)))
                .thenThrow(new QuestException(QuestErrorCode.GPS_PERMISSION_REQUIRED, HttpStatus.BAD_REQUEST, "GPS permission is required"));

        mockMvc.perform(post("/api/quests/{questId}/location-verifications", questId)
                        .header("Authorization", "Bearer session-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"permissionDenied":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GPS_PERMISSION_REQUIRED"));
    }

    @Test
    void verifyLocationReturnsQuestError() throws Exception {
        UUID questId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(questService.verifyLocation(eq("session-token"), eq(questId), any(LocationVerificationRequest.class)))
                .thenThrow(new QuestException(QuestErrorCode.LOCATION_TOO_FAR, HttpStatus.BAD_REQUEST, "Current location is outside quest hotspot"));

        mockMvc.perform(post("/api/quests/{questId}/location-verifications", questId)
                        .header("Authorization", "Bearer session-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"permissionDenied":false,"latitude":24.147000,"longitude":120.670000,"accuracyMeters":10.0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOCATION_TOO_FAR"));
    }
}
