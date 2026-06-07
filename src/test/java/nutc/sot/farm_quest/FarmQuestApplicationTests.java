package nutc.sot.farm_quest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FarmQuestApplicationTests.TestConfig.class)
class FarmQuestApplicationTests {

    @Test
    void contextLoads() {
    }

    @Configuration
    static class TestConfig {
    }
}
