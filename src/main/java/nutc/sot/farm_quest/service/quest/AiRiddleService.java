package nutc.sot.farm_quest.service.quest;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.dto.quest.AiRiddleConversationResponse;
import nutc.sot.farm_quest.dto.quest.AiRiddleMessageItem;
import nutc.sot.farm_quest.dto.quest.AiRiddleMessageRequest;
import nutc.sot.farm_quest.dto.quest.AiRiddleMessageResponse;
import nutc.sot.farm_quest.dto.quest.AiRiddleResult;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConfigEntity;
import nutc.sot.farm_quest.persistence.entity.AiRiddleConversationEntity;
import nutc.sot.farm_quest.persistence.entity.AiRiddleMessageEntity;
import nutc.sot.farm_quest.persistence.entity.QuestEntity;
import nutc.sot.farm_quest.persistence.entity.QuestProgressEntity;
import nutc.sot.farm_quest.persistence.entity.VisitorSessionEntity;
import nutc.sot.farm_quest.persistence.repository.AiRiddleConfigRepository;
import nutc.sot.farm_quest.persistence.repository.AiRiddleConversationRepository;
import nutc.sot.farm_quest.persistence.repository.AiRiddleMessageRepository;
import nutc.sot.farm_quest.persistence.repository.QuestRepository;
import nutc.sot.farm_quest.service.auth.SessionService;
import nutc.sot.farm_quest.service.coupon.CouponService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiRiddleService {

    private final SessionService                 sessionService;
    private final QuestRepository                questRepository;
    private final ProgressService                progressService;
    private final AiRiddleConfigRepository       aiRiddleConfigRepository;
    private final AiRiddleConversationRepository aiRiddleConversationRepository;
    private final AiRiddleMessageRepository      aiRiddleMessageRepository;
    private final PromptPolicyService            promptPolicyService;
    private final AnswerPolicyService            answerPolicyService;
    private final RagService                     ragService;
    private final CouponService                  couponService;
    private final ChatClient                     chatClient;
    @Value("${spring.ai.openai.chat.options.model:${SPRING_AI_CHAT_MODEL:openai}}")
    private String                               chatModelName;

    @Transactional
    public AiRiddleConversationResponse getConversation(String token, UUID questId) {
        VisitorSessionEntity       session      = sessionService.requireActiveSession(token);
        QuestEntity                quest        = requireAvailableQuest(session, questId);
        QuestProgressEntity        progress     = requireReadableProgress(session, quest);
        AiRiddleConversationEntity conversation = findConversation(session, quest, progress).orElse(null);

        if (conversation == null) {
            conversation = initializeConversation(session, quest, progress);
        }

        List<AiRiddleMessageItem> messages = aiRiddleMessageRepository.findByConversation_IdOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .map(this::toMessageItem)
                .toList();
        return new AiRiddleConversationResponse(
                quest.getId(),
                conversation.getId(),
                progress.getStatus(),
                "COMPLETED".equals(progress.getStatus()),
                nextStep(progress.getStatus()),
                messages
        );
    }

    private AiRiddleConversationEntity initializeConversation(VisitorSessionEntity session, QuestEntity quest, QuestProgressEntity progress) {
        AiRiddleConfigEntity config =
                aiRiddleConfigRepository
                        .findByQuest_IdAndStatus(quest.getId(), "ACTIVE")
                        .orElseThrow(() -> new QuestException(QuestErrorCode.AI_RIDDLE_NOT_AVAILABLE, HttpStatus.BAD_REQUEST, "AI riddle is not available"));

        AiRiddleConversationEntity conversation = createConversation(session, quest);
        progressService.markAiRiddleStarted(progress, conversation);

        OffsetDateTime now = OffsetDateTime.now();
        PromptPolicyService.PromptBundle promptBundle = promptPolicyService.build(config, quest, progress, List.of(), List.of(), null, true);

        String assistantReply;
        try {
            assistantReply = chatClient.prompt()
                    .system(promptBundle.systemPrompt())
                    .user(promptBundle.userPrompt())
                    .call()
                    .content();
        } catch (RuntimeException exception) {
            throw new QuestException(QuestErrorCode.AI_PROVIDER_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE, "AI provider is unavailable");
        }

        saveMessage(conversation, "ASSISTANT", assistantReply, modelName(), null, Map.of("source", "ai", "isOpeningMessage", true), now);
        return conversation;
    }

    @Transactional
    public AiRiddleMessageResponse sendMessage(String token, UUID questId, AiRiddleMessageRequest request) {
        VisitorSessionEntity session  = sessionService.requireActiveSession(token);
        QuestEntity          quest    = requireAvailableQuest(session, questId);
        QuestProgressEntity  progress = requireWritableProgress(session, quest);
        AiRiddleConfigEntity config =
                aiRiddleConfigRepository
                        .findByQuest_IdAndStatus(quest.getId(), "ACTIVE")
                        .orElseThrow(() -> new QuestException(QuestErrorCode.AI_RIDDLE_NOT_AVAILABLE, HttpStatus.BAD_REQUEST, "AI riddle is not available"));

        String visitorMessage = request.content() == null ? null : request.content().trim();
        if (!StringUtils.hasText(visitorMessage)) {
            throw new QuestException(QuestErrorCode.AI_RIDDLE_MESSAGE_EMPTY, HttpStatus.BAD_REQUEST, "AI riddle message is empty");
        }

        AiRiddleConversationEntity conversation = findConversation(session, quest, progress)
                .orElseThrow(() -> new QuestException(QuestErrorCode.AI_RIDDLE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR, "AI riddle conversation is not initialized"));

        OffsetDateTime now = OffsetDateTime.now();
        saveMessage(conversation, "VISITOR", visitorMessage, null, null, Map.of("source", "visitor"), now);

        List<AiRiddleMessageItem> history = aiRiddleMessageRepository.findByConversation_IdOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .map(this::toMessageItem)
                .toList();
        List<Document>                   documents    = ragService.retrieve(visitorMessage, new PromptPolicyService.PromptContext(quest, progress));
        PromptPolicyService.PromptBundle promptBundle = promptPolicyService.build(config, quest, progress, history, documents, visitorMessage, false);

        String assistantReply;
        try {
            assistantReply = chatClient.prompt()
                    .system(promptBundle.systemPrompt())
                    .user(promptBundle.userPrompt())
                    .call()
                    .content();
        } catch (RuntimeException exception) {
            throw new QuestException(QuestErrorCode.AI_PROVIDER_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE, "AI provider is unavailable");
        }

        Map<String, Object> ragMetadata = new HashMap<>();
        ragMetadata.put("documentCount", documents.size());
        ragMetadata.put("documentIds", documents.stream().map(Document::getId).toList());

        PromptPolicyService.PromptBundle judgePrompt = promptPolicyService.buildJudgePrompt(
                config,
                quest,
                progress,
                history,
                documents,
                visitorMessage,
                assistantReply
        );

        String judgeResponse;
        try {
            judgeResponse = chatClient.prompt()
                    .system(judgePrompt.systemPrompt())
                    .user(judgePrompt.userPrompt())
                    .call()
                    .content();
        } catch (RuntimeException exception) {
            judgeResponse = null;
            ragMetadata.put("judgeProviderUnavailable", true);
        }

        AiRiddleResult result = answerPolicyService.evaluate(config, assistantReply, history, visitorMessage, judgeResponse, ragMetadata);

        // Update the VISITOR message with the answer correctness
        AiRiddleMessageEntity visitorMsg = aiRiddleMessageRepository.findByConversation_IdOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .filter(msg -> msg.getRole().equals("VISITOR") && msg.getContent().equals(visitorMessage))
                .reduce((first, second) -> second) // Get the last matching message
                .orElse(null);
        if (visitorMsg != null) {
            visitorMsg.setAnswerCorrect(result.correct());
            aiRiddleMessageRepository.save(visitorMsg);
        }

        saveMessage(conversation, "ASSISTANT", result.replyContent(), modelName(), null, result.metadata(), OffsetDateTime.now());
        touchConversation(conversation, OffsetDateTime.now());

        QuestProgressEntity updatedProgress = progress;
        if (result.correct()) {
            OffsetDateTime completedAt = OffsetDateTime.now();
            updatedProgress = progressService.markCompletedFromAiRiddle(progress, conversation, completedAt);
            couponService.issueCouponForCompletedQuest(session.getVisitorAccount(), quest, completedAt);
            completeConversation(conversation, completedAt);
        }

        return new AiRiddleMessageResponse(
                quest.getId(),
                conversation.getId(),
                updatedProgress.getStatus(),
                result.replyContent(),
                result.correct(),
                "COMPLETED".equals(updatedProgress.getStatus()),
                nextStep(updatedProgress.getStatus()),
                result.correct() ? null : "請依照線索繼續作答。",
                result.judgeReason()
        );
    }

    private QuestEntity requireAvailableQuest(VisitorSessionEntity session, UUID questId) {
        QuestEntity quest =
                questRepository
                        .findById(questId)
                        .filter(item -> item.getGame().getId().equals(session.getGame().getId()))
                        .orElseThrow(() -> new QuestException(QuestErrorCode.QUEST_NOT_FOUND, HttpStatus.NOT_FOUND, "Quest not found"));

        if (!"ACTIVE".equals(quest.getStatus())) {
            throw new QuestException(QuestErrorCode.QUEST_NOT_AVAILABLE, HttpStatus.BAD_REQUEST, "Quest is not available");
        }
        return quest;
    }

    private QuestProgressEntity requireReadableProgress(VisitorSessionEntity session, QuestEntity quest) {
        QuestProgressEntity progress = progressService.findProgress(session.getVisitorAccount().getId(), quest.getId())
                .orElseThrow(() -> new QuestException(QuestErrorCode.QUEST_LOCATION_NOT_VERIFIED, HttpStatus.BAD_REQUEST, "Quest location is not verified"));
        if ("NOT_STARTED".equals(progress.getStatus()) || "STARTED".equals(progress.getStatus())) {
            throw new QuestException(QuestErrorCode.QUEST_LOCATION_NOT_VERIFIED, HttpStatus.BAD_REQUEST, "Quest location is not verified");
        }
        return progress;
    }

    private QuestProgressEntity requireWritableProgress(VisitorSessionEntity session, QuestEntity quest) {
        QuestProgressEntity progress = requireReadableProgress(session, quest);
        if ("COMPLETED".equals(progress.getStatus())) {
            throw new QuestException(QuestErrorCode.AI_RIDDLE_ALREADY_COMPLETED, HttpStatus.BAD_REQUEST, "AI riddle is already completed");
        }
        return progress;
    }

    private java.util.Optional<AiRiddleConversationEntity> findConversation(VisitorSessionEntity session, QuestEntity quest, QuestProgressEntity progress) {
        if (progress.getLastAiConversation() != null) {
            return aiRiddleConversationRepository.findByIdAndVisitorAccount_Id(progress.getLastAiConversation().getId(), session.getVisitorAccount().getId());
        }
        return aiRiddleConversationRepository.findFirstByVisitorAccount_IdAndQuest_IdAndStatusOrderByStartedAtDesc(session.getVisitorAccount().getId(), quest.getId(), "ACTIVE")
                .or(() -> aiRiddleConversationRepository.findFirstByVisitorAccount_IdAndQuest_IdOrderByStartedAtDesc(session.getVisitorAccount().getId(), quest.getId()));
    }

    private AiRiddleConversationEntity createConversation(VisitorSessionEntity session, QuestEntity quest) {
        OffsetDateTime             now          = OffsetDateTime.now();
        AiRiddleConversationEntity conversation = new AiRiddleConversationEntity();
        conversation.setId(UUID.randomUUID());
        conversation.setGame(session.getGame());
        conversation.setVisitorAccount(session.getVisitorAccount());
        conversation.setQuest(quest);
        conversation.setStatus("ACTIVE");
        conversation.setStartedAt(now);
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        conversation.setLastMessageAt(now);
        return aiRiddleConversationRepository.save(conversation);
    }

    private AiRiddleMessageEntity saveMessage(
            AiRiddleConversationEntity conversation,
            String role,
            String content,
            String aiModel,
            Boolean answerCorrect,
            Map<String, Object> metadata,
            OffsetDateTime createdAt
    ) {
        AiRiddleMessageEntity entity = new AiRiddleMessageEntity();
        entity.setId(UUID.randomUUID());
        entity.setConversation(conversation);
        entity.setRole(role);
        entity.setContent(content);
        entity.setAiModel(aiModel);
        entity.setAnswerCorrect(answerCorrect);
        entity.setMetadata(metadata == null ? Map.of() : metadata);
        entity.setCreatedAt(createdAt);
        return aiRiddleMessageRepository.save(entity);
    }

    private void touchConversation(AiRiddleConversationEntity conversation, OffsetDateTime time) {
        conversation.setLastMessageAt(time);
        conversation.setUpdatedAt(time);
        aiRiddleConversationRepository.save(conversation);
    }

    private void completeConversation(AiRiddleConversationEntity conversation, OffsetDateTime time) {
        conversation.setStatus("COMPLETED");
        conversation.setCompletedAt(time);
        conversation.setClosedAt(time);
        conversation.setLastMessageAt(time);
        conversation.setUpdatedAt(time);
        aiRiddleConversationRepository.save(conversation);
    }

    private AiRiddleMessageItem toMessageItem(AiRiddleMessageEntity entity) {
        return new AiRiddleMessageItem(
                entity.getId(),
                entity.getRole(),
                entity.getContent(),
                entity.getAnswerCorrect(),
                entity.getCreatedAt()
        );
    }

    private String nextStep(String progressStatus) {
        return switch (progressStatus) {
            case "LOCATION_VERIFIED", "AI_RIDDLE_STARTED", "COMPLETED" -> "AI_RIDDLE_AVAILABLE";
            case "STARTED" -> "VERIFY_LOCATION";
            default -> "START_QUEST";
        };
    }

    private String modelName() {
        return chatModelName;
    }
}
