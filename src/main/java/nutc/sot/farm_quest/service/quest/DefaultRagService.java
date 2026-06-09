package nutc.sot.farm_quest.service.quest;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultRagService implements RagService {

    private final VectorStoreService vectorStoreService;

    @Override
    public List<Document> retrieve(String message, PromptPolicyService.PromptContext context) {
        return vectorStoreService.search(message, context);
    }
}
