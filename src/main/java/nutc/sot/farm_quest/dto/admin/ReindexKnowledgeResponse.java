package nutc.sot.farm_quest.dto.admin;

public record ReindexKnowledgeResponse(
        boolean accepted,
        int queuedDocumentCount,
        String status
) {
}
