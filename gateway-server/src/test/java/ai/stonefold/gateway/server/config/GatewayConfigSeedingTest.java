package ai.stonefold.gateway.server.config;

import ai.stonefold.gateway.core.TenantId;
import ai.stonefold.gateway.server.config.TenantSeedProperties.TenantSeed;
import ai.stonefold.gateway.tenancy.ApiKeys;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewayConfigSeedingTest {

    @Test
    void configuredTenantsAreSeededWithTheirKeyHashes() {
        String keyA = ApiKeys.generate();
        String keyB = ApiKeys.generate();
        var seeds = new TenantSeedProperties(List.of(
                new TenantSeed("acme", "Acme Corp", null, List.of(ApiKeys.hash(keyA))),
                new TenantSeed("globex", null, null, List.of(ApiKeys.hash(keyB)))));

        var registry = GatewayConfig.buildRegistry(seeds);

        assertThat(registry.authenticate(keyA))
                .hasValueSatisfying(t -> assertThat(t.id().value()).isEqualTo("acme"));
        assertThat(registry.authenticate(keyB))
                .hasValueSatisfying(t -> assertThat(t.id().value()).isEqualTo("globex"));
        // displayName defaults to the id when not configured
        assertThat(registry.find(new TenantId("globex")))
                .hasValueSatisfying(t -> assertThat(t.displayName()).isEqualTo("globex"));
    }

    @Test
    void duplicateTenantIdRefusesToBuild() {
        var seeds = new TenantSeedProperties(List.of(
                new TenantSeed("acme", null, null, List.of(ApiKeys.hash(ApiKeys.generate()))),
                new TenantSeed("acme", null, null, List.of(ApiKeys.hash(ApiKeys.generate())))));

        assertThatThrownBy(() -> GatewayConfig.buildRegistry(seeds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("duplicate tenant id");
    }

    @Test
    void tenantWithoutKeyHashesRefusesToBuild() {
        var seeds = new TenantSeedProperties(List.of(
                new TenantSeed("acme", null, null, List.of())));

        assertThatThrownBy(() -> GatewayConfig.buildRegistry(seeds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no key-hashes");
    }

    @Test
    void plaintextKeyInsteadOfHashRefusesWithPointedError() {
        var seeds = new TenantSeedProperties(List.of(
                new TenantSeed("acme", null, null, List.of(ApiKeys.generate()))));

        assertThatThrownBy(() -> GatewayConfig.buildRegistry(seeds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("plaintext API key");
    }

    @Test
    void malformedHashRefusesToBuild() {
        var seeds = new TenantSeedProperties(List.of(
                new TenantSeed("acme", null, null, List.of("not-a-hash"))));

        assertThatThrownBy(() -> GatewayConfig.buildRegistry(seeds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("malformed key hash");
    }

    @Test
    void duplicateHashAcrossTenantsRefusesToBuild() {
        String hash = ApiKeys.hash(ApiKeys.generate());
        var seeds = new TenantSeedProperties(List.of(
                new TenantSeed("tenant-a", null, null, List.of(hash)),
                new TenantSeed("tenant-b", null, null, List.of(hash))));

        assertThatThrownBy(() -> GatewayConfig.buildRegistry(seeds))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blankTenantIdRefusesToBuild() {
        var seeds = new TenantSeedProperties(List.of(
                new TenantSeed(" ", null, null, List.of(ApiKeys.hash(ApiKeys.generate())))));

        assertThatThrownBy(() -> GatewayConfig.buildRegistry(seeds))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emptyConfigAutoSeedsTheDevTenant() {
        var registry = GatewayConfig.buildRegistry(new TenantSeedProperties(List.of()));

        assertThat(registry.find(new TenantId("dev"))).isPresent();
    }

    @Test
    void configuredTenantsSuppressTheAutoSeed() {
        var seeds = new TenantSeedProperties(List.of(
                new TenantSeed("acme", null, null, List.of(ApiKeys.hash(ApiKeys.generate())))));

        var registry = GatewayConfig.buildRegistry(seeds);

        assertThat(registry.find(new TenantId("dev"))).isEmpty();
    }
}
