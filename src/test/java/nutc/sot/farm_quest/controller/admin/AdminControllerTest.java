package nutc.sot.farm_quest.controller.admin;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import nutc.sot.farm_quest.dto.admin.AdminOverviewStatsResponse;
import nutc.sot.farm_quest.dto.admin.AdminQuestListResponse;
import nutc.sot.farm_quest.dto.admin.AdminQuestResponse;
import nutc.sot.farm_quest.dto.admin.ReindexKnowledgeResponse;
import nutc.sot.farm_quest.exception.AuthErrorCode;
import nutc.sot.farm_quest.exception.AuthException;
import nutc.sot.farm_quest.exception.GlobalExceptionHandler;
import nutc.sot.farm_quest.service.admin.AdminAiRiddleService;
import nutc.sot.farm_quest.service.admin.AdminAuthService;
import nutc.sot.farm_quest.service.admin.AdminCouponService;
import nutc.sot.farm_quest.service.admin.AdminKnowledgeService;
import nutc.sot.farm_quest.service.admin.AdminLocationService;
import nutc.sot.farm_quest.service.admin.AdminQuestService;
import nutc.sot.farm_quest.service.admin.AdminStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerTest {

    private final AdminAuthService adminAuthService = mock(AdminAuthService.class);
    private final AdminQuestService adminQuestService = mock(AdminQuestService.class);
    private final AdminLocationService adminLocationService = mock(AdminLocationService.class);
    private final AdminAiRiddleService adminAiRiddleService = mock(AdminAiRiddleService.class);
    private final AdminCouponService adminCouponService = mock(AdminCouponService.class);
    private final AdminKnowledgeService adminKnowledgeService = mock(AdminKnowledgeService.class);
    private final AdminStatisticsService adminStatisticsService = mock(AdminStatisticsService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AdminController(
                    adminAuthService,
                    adminQuestService,
                    adminLocationService,
                    adminAiRiddleService,
                    adminCouponService,
                    adminKnowledgeService,
                    adminStatisticsService
            ))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void getQuestsReturnsContractForValidAdminSecret() throws Exception {
        when(adminQuestService.getQuests()).thenReturn(new AdminQuestListResponse(List.of(
                new AdminQuestResponse(
                        UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        "tea-quest-01",
                        "茶園謎題",
                        "示範任務",
                        1,
                        "ACTIVE",
                        OffsetDateTime.parse("2026-06-09T10:00:00+08:00")
                )
        )));

        mockMvc.perform(get("/api/admin/quests")
                        .header("Authorization", "Bearer admin-secret"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.quests.length()").value(1))
                .andExpect(jsonPath("$.quests[0].code").value("tea-quest-01"));

        verify(adminAuthService).requireAdmin("admin-secret");
    }

    @Test
    void getQuestsRejectsMissingAuthorization() throws Exception {
        doThrow(new AuthException(AuthErrorCode.ADMIN_UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Admin authorization failed"))
                .when(adminAuthService)
                .requireAdmin(null);

        mockMvc.perform(get("/api/admin/quests"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ADMIN_UNAUTHORIZED"));
    }

    @Test
    void getOverviewStatsRejectsVisitorSessionToken() throws Exception {
        doThrow(new AuthException(AuthErrorCode.ADMIN_UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Admin authorization failed"))
                .when(adminAuthService)
                .requireAdmin("visitor-session-token");

        mockMvc.perform(get("/api/admin/stats/overview")
                        .header("Authorization", "Bearer visitor-session-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ADMIN_UNAUTHORIZED"));
    }

    @Test
    void reindexKnowledgeUsesTargetedReindexWhenRequestBodyIsMissing() throws Exception {
        when(adminKnowledgeService.reindexKnowledge(any())).thenReturn(new ReindexKnowledgeResponse(true, 0, "REINDEX_QUEUED"));

        mockMvc.perform(post("/api/admin/knowledge-documents/reindex")
                        .header("Authorization", "Bearer admin-secret"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accepted").value(true))
                .andExpect(jsonPath("$.queuedDocumentCount").value(0))
                .andExpect(jsonPath("$.status").value("REINDEX_QUEUED"));

        verify(adminAuthService).requireAdmin("admin-secret");
        verify(adminKnowledgeService).reindexKnowledge(any());
    }

    @Test
    void getOverviewStatsReturnsContract() throws Exception {
        when(adminStatisticsService.getOverviewStats()).thenReturn(new AdminOverviewStatsResponse(5, 3, 2, 0.6666667));

        mockMvc.perform(get("/api/admin/stats/overview")
                        .header("Authorization", "Bearer admin-secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedQuestCount").value(5))
                .andExpect(jsonPath("$.issuedCouponCount").value(3))
                .andExpect(jsonPath("$.usedCouponCount").value(2));
    }
}
