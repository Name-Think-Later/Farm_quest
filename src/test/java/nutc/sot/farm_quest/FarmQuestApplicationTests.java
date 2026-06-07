package nutc.sot.farm_quest;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(FarmQuestApplicationTests.TestOverrides.class)
class FarmQuestApplicationTests {

    @MockitoBean
    private DataSource dataSource;

    @MockitoBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockitoBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void contextLoads() {
    }

    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
    static class TestOverrides {
    }
}
