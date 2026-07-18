// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.server.config;

import ai.stonefold.gateway.tenancy.ApiKeys;
import ai.stonefold.gateway.tenancy.TenantRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the whole seeding path through Spring's property binding: yaml-style
 * properties -> TenantSeedProperties -> registry -> authenticate.
 */
@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:seedbind;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
@ActiveProfiles("local")
class TenantSeedBindingTest {

    // Any string hashes fine; a fixed one keeps the test deterministic.
    private static final String KEY = "sfk_fixed-test-key-for-binding";

    @DynamicPropertySource
    static void seedProperties(DynamicPropertyRegistry registry) {
        registry.add("stonefold.tenants[0].id", () -> "acme");
        registry.add("stonefold.tenants[0].display-name", () -> "Acme Corp");
        registry.add("stonefold.tenants[0].key-hashes[0]", () -> ApiKeys.hash(KEY));
    }

    @Autowired
    private TenantRegistry registry;

    @Test
    void keyFromBoundConfigAuthenticatesItsTenant() {
        assertThat(registry.authenticate(KEY))
                .hasValueSatisfying(t -> {
                    assertThat(t.id().value()).isEqualTo("acme");
                    assertThat(t.displayName()).isEqualTo("Acme Corp");
                });
    }

    @Test
    void unknownKeyStillMisses() {
        assertThat(registry.authenticate(ApiKeys.generate())).isEmpty();
    }
}
