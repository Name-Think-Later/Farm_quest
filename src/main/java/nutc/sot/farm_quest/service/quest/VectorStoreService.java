package nutc.sot.farm_quest.service.quest;

import java.util.List;
import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import org.springframework.ai.document.Document;

public interface VectorStoreService {

    List<Document> search(String query, PromptPolicyService.PromptContext context);

    void indexKnowledgeDocuments(List<KnowledgeDocumentEntity> knowledgeDocuments);
}
