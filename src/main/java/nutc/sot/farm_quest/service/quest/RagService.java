package nutc.sot.farm_quest.service.quest;

import java.util.List;
import org.springframework.ai.document.Document;

public interface RagService {

    List<Document> retrieve(String message, PromptPolicyService.PromptContext context);
}
