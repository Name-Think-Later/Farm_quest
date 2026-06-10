package nutc.sot.farm_quest.dto.admin;

public record ReindexKnowledgeRequest(
        boolean fullRebuild,
        String mode
) {

    public ReindexKnowledgeRequest(boolean fullRebuild) {
        this(fullRebuild, null);
    }
}
