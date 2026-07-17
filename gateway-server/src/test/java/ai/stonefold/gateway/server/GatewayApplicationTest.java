package ai.stonefold.gateway.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Smoke test: the full local wiring must start with no external services. */
@SpringBootTest
@ActiveProfiles("local")
class GatewayApplicationTest {

    @Test
    void contextLoads() {
    }
}
