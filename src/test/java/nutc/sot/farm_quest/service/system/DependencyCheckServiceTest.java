package nutc.sot.farm_quest.service.system;

import javax.sql.DataSource;
import nutc.sot.farm_quest.config.QdrantProperties;
import nutc.sot.farm_quest.service.quest.EmbeddingService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DependencyCheckServiceTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final RedisConnectionFactory redisConnectionFactory = mock(RedisConnectionFactory.class);
    private final RestClient qdrantRestClient = RestClient.builder().baseUrl("http://localhost:6333").build();
    private final ChatClient chatClient = mock(ChatClient.class);
    private final EmbeddingService embeddingService = mock(EmbeddingService.class);

    @Test
    void checkDependenciesMarksAiModelsConfiguredWhenRequiredSettingsExist() {
        Environment environment = new MockEnvironment()
                .withProperty("spring.ai.chat.provider", "openai")
                .withProperty("spring.ai.embedding.provider", "openai")
                .withProperty("spring.ai.openai.base-url", "http://chat-proxy")
                .withProperty("spring.ai.openai.api-key", "chat-key")
                .withProperty("spring.ai.openai.chat.model", "gpt-4o-mini")
                .withProperty("spring.ai.openai.embedding.base-url", "http://embedding-proxy")
                .withProperty("spring.ai.openai.embedding.api-key", "embedding-key")
                .withProperty("spring.ai.openai.embedding.model", "bge-m3");
        DependencyCheckService service = new DependencyCheckService(
                dataSource,
                redisConnectionFactory,
                new QdrantProperties(false, "http://localhost:6333", "", "test", 1536, "COSINE"),
                qdrantRestClient,
                environment,
                chatClient,
                embeddingService
        );

        var response = service.checkDependencies();

        assertThat(response.dependencies())
                .anySatisfy(item -> {
                    assertThat(item.name()).isEqualTo("AI Chat Model");
                    assertThat(item.status()).isEqualTo("CONFIGURED");
                    assertThat(item.message()).contains("model=gpt-4o-mini");
                })
                .anySatisfy(item -> {
                    assertThat(item.name()).isEqualTo("AI Embedding Model");
                    assertThat(item.status()).isEqualTo("CONFIGURED");
                    assertThat(item.message()).contains("model=bge-m3");
                });
    }

    @Test
    void checkDependenciesMarksAiModelsMissingWhenApiKeyIsAbsent() {
        Environment environment = new MockEnvironment()
                .withProperty("spring.ai.chat.provider", "openai")
                .withProperty("spring.ai.embedding.provider", "openai")
                .withProperty("spring.ai.openai.base-url", "http://chat-proxy")
                .withProperty("spring.ai.openai.chat.model", "gpt-4o-mini")
                .withProperty("spring.ai.openai.embedding.base-url", "http://embedding-proxy")
                .withProperty("spring.ai.openai.embedding.model", "bge-m3");
        DependencyCheckService service = new DependencyCheckService(
                dataSource,
                redisConnectionFactory,
                new QdrantProperties(false, "http://localhost:6333", "", "test", 1536, "COSINE"),
                qdrantRestClient,
                environment,
                chatClient,
                embeddingService
        );

        var response = service.checkDependencies();

        assertThat(response.dependencies())
                .anySatisfy(item -> {
                    assertThat(item.name()).isEqualTo("AI Chat Model");
                    assertThat(item.status()).isEqualTo("MISSING");
                })
                .anySatisfy(item -> {
                    assertThat(item.name()).isEqualTo("AI Embedding Model");
                    assertThat(item.status()).isEqualTo("MISSING");
                });
    }

    @Test
    void probeAiDependenciesReturnsUpWhenChatAndEmbeddingSucceed() {
        Environment environment = new MockEnvironment()
                .withProperty("spring.ai.chat.provider", "openai")
                .withProperty("spring.ai.embedding.provider", "openai")
                .withProperty("spring.ai.openai.base-url", "http://chat-proxy")
                .withProperty("spring.ai.openai.api-key", "chat-key")
                .withProperty("spring.ai.openai.chat.model", "gpt-4o-mini")
                .withProperty("spring.ai.openai.embedding.base-url", "http://embedding-proxy")
                .withProperty("spring.ai.openai.embedding.api-key", "embedding-key")
                .withProperty("spring.ai.openai.embedding.model", "bge-m3");
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user("reply with ok")).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("ok");
        when(embeddingService.model()).thenReturn(embeddingModel);
        when(embeddingModel.embed("farm quest probe")).thenReturn(new float[]{0.1f, 0.2f});
        DependencyCheckService service = new DependencyCheckService(
                dataSource,
                redisConnectionFactory,
                new QdrantProperties(false, "http://localhost:6333", "", "test", 1536, "COSINE"),
                qdrantRestClient,
                environment,
                chatClient,
                embeddingService
        );

        var response = service.probeAiDependencies();

        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.dependencies())
                .anySatisfy(item -> {
                    assertThat(item.name()).isEqualTo("AI Chat Model");
                    assertThat(item.status()).isEqualTo("UP");
                })
                .anySatisfy(item -> {
                    assertThat(item.name()).isEqualTo("AI Embedding Model");
                    assertThat(item.status()).isEqualTo("UP");
                });
    }
}
