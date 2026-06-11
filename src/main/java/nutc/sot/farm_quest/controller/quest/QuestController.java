package nutc.sot.farm_quest.controller.quest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.common.ApiResponse;
import nutc.sot.farm_quest.dto.quest.AiRiddleConversationResponse;
import nutc.sot.farm_quest.dto.quest.AiRiddleMessageRequest;
import nutc.sot.farm_quest.dto.quest.AiRiddleMessageResponse;
import nutc.sot.farm_quest.dto.quest.LocationHintResponse;
import nutc.sot.farm_quest.dto.quest.LocationVerificationRequest;
import nutc.sot.farm_quest.dto.quest.LocationVerificationResponse;
import nutc.sot.farm_quest.dto.quest.QuestDetailResponse;
import nutc.sot.farm_quest.dto.quest.QuestListResponse;
import nutc.sot.farm_quest.dto.quest.StartQuestResponse;
import nutc.sot.farm_quest.service.quest.AiRiddleService;
import nutc.sot.farm_quest.service.quest.QuestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;
    private final AiRiddleService aiRiddleService;

    @GetMapping
    public ResponseEntity<ApiResponse<QuestListResponse>> getQuests(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(questService.getVisibleQuests(extractBearerToken(request))));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<QuestDetailResponse>> getCurrentQuest(HttpServletRequest request) {
        QuestDetailResponse quest = questService.getCurrentQuest(extractBearerToken(request));
        return ResponseEntity.ok(ApiResponse.success(quest));
    }

    @PostMapping("/{questId}/start")
    public ResponseEntity<ApiResponse<StartQuestResponse>> startQuest(@PathVariable UUID questId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(questService.startQuest(extractBearerToken(request), questId)));
    }

    @GetMapping("/{questId}/location-hint")
    public ResponseEntity<ApiResponse<LocationHintResponse>> getLocationHint(@PathVariable UUID questId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(questService.getLocationHint(extractBearerToken(request), questId)));
    }

    @PostMapping("/{questId}/location-verifications")
    public ResponseEntity<ApiResponse<LocationVerificationResponse>> verifyLocation(@PathVariable UUID questId,
                                                                                     @Valid @RequestBody LocationVerificationRequest request,
                                                                                     HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(ApiResponse.success(questService.verifyLocation(extractBearerToken(httpServletRequest), questId, request)));
    }

    @GetMapping("/{questId}/ai-riddle/messages")
    public ResponseEntity<ApiResponse<AiRiddleConversationResponse>> getAiRiddleMessages(@PathVariable UUID questId,
                                                                                          HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiRiddleService.getConversation(extractBearerToken(request), questId)));
    }

    @PostMapping("/{questId}/ai-riddle/messages")
    public ResponseEntity<ApiResponse<AiRiddleMessageResponse>> sendAiRiddleMessage(@PathVariable UUID questId,
                                                                                     @Valid @RequestBody AiRiddleMessageRequest request,
                                                                                     HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(ApiResponse.success(aiRiddleService.sendMessage(extractBearerToken(httpServletRequest), questId, request)));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
