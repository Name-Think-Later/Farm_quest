package nutc.sot.farm_quest.service.quest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import nutc.sot.farm_quest.persistence.entity.KnowledgeDocumentEntity;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeChunkingService {

    private static final List<Character> PUNCTUATION_MARKS = List.of('.', ',', ';', ':', '!', '?', '，', '。', '；', '：', '！', '？', '\n');

    private final TokenTextSplitter tokenTextSplitter;

    public KnowledgeChunkingService() {
        this(new TokenTextSplitter(400, 150, 20, 50, true, PUNCTUATION_MARKS));
    }

    KnowledgeChunkingService(TokenTextSplitter tokenTextSplitter) {
        this.tokenTextSplitter = tokenTextSplitter;
    }

    public List<Document> chunk(KnowledgeDocumentEntity knowledgeDocument) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", knowledgeDocument.getId().toString());
        metadata.put("gameId", knowledgeDocument.getGame().getId().toString());
        metadata.put("questId", knowledgeDocument.getQuest() == null ? "" : knowledgeDocument.getQuest().getId().toString());
        metadata.put("locationId", knowledgeDocument.getLocation() == null ? "" : knowledgeDocument.getLocation().getId().toString());
        metadata.put("title", knowledgeDocument.getTitle());
        metadata.put("source", knowledgeDocument.getSource());
        metadata.put("spoilerLevel", knowledgeDocument.getSpoilerLevel());
        metadata.put("version", knowledgeDocument.getVersion().toString());

        Document sourceDocument = new Document(knowledgeDocument.getId().toString(), knowledgeText(knowledgeDocument), metadata);
        List<Document> rawChunks = tokenTextSplitter.apply(List.of(sourceDocument));
        if (rawChunks.isEmpty()) {
            rawChunks = List.of(sourceDocument);
        }

        List<Document> chunkedDocuments = new ArrayList<>();
        for (int i = 0; i < rawChunks.size(); i++) {
            Document rawChunk = rawChunks.get(i);
            Map<String, Object> chunkMetadata = new HashMap<>(rawChunk.getMetadata());
            chunkMetadata.put("chunkIndex", i);
            chunkedDocuments.add(new Document(chunkId(knowledgeDocument.getId(), i), rawChunk.getText(), chunkMetadata));
        }
        return chunkedDocuments;
    }

    private String knowledgeText(KnowledgeDocumentEntity knowledgeDocument) {
        String content = knowledgeDocument.getContent();
        if (content == null || content.isBlank()) {
            return knowledgeDocument.getTitle();
        }
        return knowledgeDocument.getTitle() + "\n\n" + content.trim();
    }

    private String chunkId(UUID knowledgeDocumentId, int chunkIndex) {
        return UUID.nameUUIDFromBytes((knowledgeDocumentId + ":" + chunkIndex).getBytes(StandardCharsets.UTF_8)).toString();
    }
}
