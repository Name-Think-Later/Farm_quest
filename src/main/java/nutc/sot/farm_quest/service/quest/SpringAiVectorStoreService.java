package nutc.sot.farm_quest.service.quest;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nutc.sot.farm_quest.exception.QuestErrorCode;
import nutc.sot.farm_quest.exception.QuestException;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpringAiVectorStoreService implements VectorStoreService {

    private final VectorStore vectorStore;

    @Override
    public List<Document> search(String query, PromptPolicyService.PromptContext context) {
        try {
            SearchRequest request = SearchRequest.builder().query(query).topK(4).build();
            return vectorStore.similaritySearch(request);
        } catch (RuntimeException exception) {
            throw new QuestException(QuestErrorCode.RAG_RETRIEVAL_FAILED, HttpStatus.SERVICE_UNAVAILABLE, "RAG retrieval failed");
        }
    }
}
