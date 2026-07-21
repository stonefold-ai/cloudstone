// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: the full local wiring must start with no external services.
 * A tenant is seeded via properties so the dev auto-seed (and its one-time
 * key printout) stays out of test and CI logs.
 */
@SpringBootTest(properties = {
        "cloudstone.tenants[0].id=smoke",
        "cloudstone.tenants[0].key-hashes[0]=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
})
@ActiveProfiles("local")
class GatewayApplicationTest {

    @Test
    void contextLoads() {
    }
}
