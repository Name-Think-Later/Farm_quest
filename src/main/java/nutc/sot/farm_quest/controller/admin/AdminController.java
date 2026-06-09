package nutc.sot.farm_quest.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.admin.AdminAiRiddleConfigListResponse;
import nutc.sot.farm_quest.dto.admin.AdminAiRiddleConfigRequest;
import nutc.sot.farm_quest.dto.admin.AdminAiRiddleConfigResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponCampaignListResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponCampaignRequest;
import nutc.sot.farm_quest.dto.admin.AdminCouponCampaignResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponCampaignStatsListResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponListResponse;
import nutc.sot.farm_quest.dto.admin.AdminCouponUsageListResponse;
import nutc.sot.farm_quest.dto.admin.AdminLocationHotspotRequest;
import nutc.sot.farm_quest.dto.admin.AdminLocationListResponse;
import nutc.sot.farm_quest.dto.admin.AdminLocationResponse;
import nutc.sot.farm_quest.dto.admin.AdminOverviewStatsResponse;
import nutc.sot.farm_quest.dto.admin.AdminQuestCompletionStatsListResponse;
import nutc.sot.farm_quest.dto.admin.AdminQuestListResponse;
import nutc.sot.farm_quest.dto.admin.AdminQuestRequest;
import nutc.sot.farm_quest.dto.admin.AdminQuestResponse;
import nutc.sot.farm_quest.dto.admin.KnowledgeDocumentListResponse;
import nutc.sot.farm_quest.dto.admin.KnowledgeDocumentRequest;
import nutc.sot.farm_quest.dto.admin.KnowledgeDocumentResponse;
import nutc.sot.farm_quest.dto.admin.ReindexKnowledgeRequest;
import nutc.sot.farm_quest.dto.admin.ReindexKnowledgeResponse;
import nutc.sot.farm_quest.service.admin.AdminAiRiddleService;
import nutc.sot.farm_quest.service.admin.AdminAuthService;
import nutc.sot.farm_quest.service.admin.AdminCouponService;
import nutc.sot.farm_quest.service.admin.AdminKnowledgeService;
import nutc.sot.farm_quest.service.admin.AdminLocationService;
import nutc.sot.farm_quest.service.admin.AdminQuestService;
import nutc.sot.farm_quest.service.admin.AdminStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAuthService adminAuthService;
    private final AdminQuestService adminQuestService;
    private final AdminLocationService adminLocationService;
    private final AdminAiRiddleService adminAiRiddleService;
    private final AdminCouponService adminCouponService;
    private final AdminKnowledgeService adminKnowledgeService;
    private final AdminStatisticsService adminStatisticsService;

    @GetMapping("/quests")
    public ResponseEntity<AdminQuestListResponse> getQuests(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(adminQuestService.getQuests());
    }

    @PostMapping("/quests")
    public ResponseEntity<AdminQuestResponse> createQuest(@Valid @RequestBody AdminQuestRequest request,
                                                          HttpServletRequest httpServletRequest) {
        requireAdmin(httpServletRequest);
        return ResponseEntity.ok(adminQuestService.createQuest(request));
    }

    @PutMapping("/quests/{questId}")
    public ResponseEntity<AdminQuestResponse> updateQuest(@PathVariable UUID questId,
                                                          @Valid @RequestBody AdminQuestRequest request,
                                                          HttpServletRequest httpServletRequest) {
        requireAdmin(httpServletRequest);
        return ResponseEntity.ok(adminQuestService.updateQuest(questId, request));
    }

    @GetMapping("/locations")
    public ResponseEntity<AdminLocationListResponse> getLocations(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(adminLocationService.getLocations());
    }

    @PutMapping("/locations/{locationId}/hotspot")
    public ResponseEntity<AdminLocationResponse> updateLocationHotspot(@PathVariable UUID locationId,
                                                                       @Valid @RequestBody AdminLocationHotspotRequest request,
                                                                       HttpServletRequest httpServletRequest) {
        requireAdmin(httpServletRequest);
        return ResponseEntity.ok(adminLocationService.updateHotspot(locationId, request));
    }

    @GetMapping("/ai-riddles")
    public ResponseEntity<AdminAiRiddleConfigListResponse> getAiRiddles(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(adminAiRiddleService.getAiRiddles());
    }

    @PutMapping("/ai-riddles/{questId}")
    public ResponseEntity<AdminAiRiddleConfigResponse> updateAiRiddle(@PathVariable UUID questId,
                                                                      @Valid @RequestBody AdminAiRiddleConfigRequest request,
                                                                      HttpServletRequest httpServletRequest) {
        requireAdmin(httpServletRequest);
        return ResponseEntity.ok(adminAiRiddleService.upsertAiRiddleConfig(questId, request));
    }

    @GetMapping("/coupon-campaigns")
    public ResponseEntity<AdminCouponCampaignListResponse> getCouponCampaigns(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(adminCouponService.getCouponCampaigns());
    }

    @PostMapping("/coupon-campaigns")
    public ResponseEntity<AdminCouponCampaignResponse> createCouponCampaign(@Valid @RequestBody AdminCouponCampaignRequest request,
                                                                            HttpServletRequest httpServletRequest) {
        requireAdmin(httpServletRequest);
        return ResponseEntity.ok(adminCouponService.createCouponCampaign(request));
    }

    @GetMapping("/knowledge-documents")
    public ResponseEntity<KnowledgeDocumentListResponse> getKnowledgeDocuments(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(adminKnowledgeService.getKnowledgeDocuments());
    }

    @PostMapping("/knowledge-documents")
    public ResponseEntity<KnowledgeDocumentResponse> createKnowledgeDocument(@Valid @RequestBody KnowledgeDocumentRequest request,
                                                                             HttpServletRequest httpServletRequest) {
        requireAdmin(httpServletRequest);
        return ResponseEntity.ok(adminKnowledgeService.createKnowledgeDocument(request));
    }

    @PostMapping("/knowledge-documents/reindex")
    public ResponseEntity<ReindexKnowledgeResponse> reindexKnowledge(@RequestBody(required = false) ReindexKnowledgeRequest request,
                                                                     HttpServletRequest httpServletRequest) {
        requireAdmin(httpServletRequest);
        return ResponseEntity.ok(adminKnowledgeService.reindexKnowledge(request == null ? new ReindexKnowledgeRequest(false) : request));
    }

    @GetMapping("/coupons")
    public ResponseEntity<AdminCouponListResponse> getCoupons(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(adminCouponService.getCoupons());
    }

    @GetMapping("/coupon-usages")
    public ResponseEntity<AdminCouponUsageListResponse> getCouponUsages(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(adminCouponService.getCouponUsages());
    }

    @GetMapping("/stats/overview")
    public ResponseEntity<AdminOverviewStatsResponse> getOverviewStats(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(adminStatisticsService.getOverviewStats());
    }

    @GetMapping("/stats/quests")
    public ResponseEntity<AdminQuestCompletionStatsListResponse> getQuestStats(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(adminStatisticsService.getQuestCompletionStats());
    }

    @GetMapping("/stats/coupons")
    public ResponseEntity<AdminCouponCampaignStatsListResponse> getCouponStats(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(adminStatisticsService.getCouponCampaignStats());
    }

    private void requireAdmin(HttpServletRequest request) {
        adminAuthService.requireAdmin(extractBearerToken(request));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
