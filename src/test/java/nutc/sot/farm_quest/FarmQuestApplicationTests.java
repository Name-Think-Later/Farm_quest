package nutc.sot.farm_quest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration"
})
@TestPropertySource(properties = {
        "spring.application.name=Farm quest",
        "spring.docker.compose.enabled=false",
        "app.datasource.url=jdbc:postgresql://localhost:5432/farm_quest",
        "app.datasource.username=postgres",
        "app.datasource.password=postgres",
        "app.qdrant.url=http://localhost:6333",
        "app.qdrant.collection-name=knowledge-documents",
        "app.qdrant.vector-size=1536",
        "app.qdrant.initialize-on-startup=false",
        "app.ai.provider.name=spring-ai",
        "app.ai.provider.api-key=test-key"
})
class FarmQuestApplicationTests {

    @MockitoBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockitoBean
    private RestClient.Builder restClientBuilder;

    @Test
    void contextLoads() {
    }
}
