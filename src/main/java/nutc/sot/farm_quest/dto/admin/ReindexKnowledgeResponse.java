package nutc.sot.farm_quest.dto.admin;

public record ReindexKnowledgeResponse(
        boolean accepted,
        int queuedDocumentCount,
        String status,
        String requestedMode,
        String effectiveMode,
        int pendingDocumentCount,
        int failedDocumentCount
) {

    public ReindexKnowledgeResponse(boolean accepted, int queuedDocumentCount, String status) {
        this(accepted, queuedDocumentCount, status, null, null, 0, 0);
    }
}
