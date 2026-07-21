// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.tenancy;

import ai.stonefold.cloudstone.runtime.TenantId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryTenantRegistryTest {

    private static Tenant tenant(String id) {
        return new Tenant(new TenantId(id), id, null, Map.of());
    }

    @Test
    void generatedKeysHaveExpectedShape() {
        String key = ApiKeys.generate();
        assertThat(key).startsWith(ApiKeys.PREFIX);
        // 32 random bytes -> 43 base64url chars, no padding, url-safe alphabet
        assertThat(key.substring(ApiKeys.PREFIX.length()))
                .hasSize(43)
                .matches("[A-Za-z0-9_-]+");
        assertThat(ApiKeys.generate()).isNotEqualTo(key);
    }

    @Test
    void hashingIsDeterministicAndKeySensitive() {
        String key = ApiKeys.generate();
        assertThat(ApiKeys.hash(key)).isEqualTo(ApiKeys.hash(key));
        assertThat(ApiKeys.hash(key)).isNotEqualTo(ApiKeys.hash(key + "x"));
    }

    @Test
    void hashShapeCheckAcceptsRealHashesAndRejectsKeysAndGarbage() {
        String key = ApiKeys.generate();
        assertThat(ApiKeys.isHashShaped(ApiKeys.hash(key))).isTrue();
        assertThat(ApiKeys.isHashShaped(key)).isFalse();          // plaintext key, wrong length
        assertThat(ApiKeys.isHashShaped("not-a-hash")).isFalse();
        assertThat(ApiKeys.isHashShaped("")).isFalse();
        assertThat(ApiKeys.isHashShaped(null)).isFalse();
        // right length, illegal alphabet (standard base64 '+')
        assertThat(ApiKeys.isHashShaped("+".repeat(43))).isFalse();
    }

    @Test
    void validKeyResolvesItsTenant() {
        var registry = new InMemoryTenantRegistry();
        String key = ApiKeys.generate();
        registry.register(tenant("acme"), ApiKeys.hash(key));

        assertThat(registry.authenticate(key))
                .hasValueSatisfying(t -> assertThat(t.id().value()).isEqualTo("acme"));
    }

    @Test
    void multipleActiveKeysAllResolveTheSameTenant() {
        var registry = new InMemoryTenantRegistry();
        String oldKey = ApiKeys.generate();
        String newKey = ApiKeys.generate();
        registry.register(tenant("acme"), ApiKeys.hash(oldKey), ApiKeys.hash(newKey));

        assertThat(registry.authenticate(oldKey)).isPresent();
        assertThat(registry.authenticate(newKey)).isPresent();
    }

    @Test
    void revokedKeyStopsResolvingWithoutAffectingOtherKeys() {
        var registry = new InMemoryTenantRegistry();
        String oldKey = ApiKeys.generate();
        String newKey = ApiKeys.generate();
        registry.register(tenant("acme"), ApiKeys.hash(oldKey), ApiKeys.hash(newKey));

        registry.revokeKey(ApiKeys.hash(oldKey));

        assertThat(registry.authenticate(oldKey)).isEmpty();
        assertThat(registry.authenticate(newKey)).isPresent();
    }

    @Test
    void keysNeverResolveAcrossTenants() {
        var registry = new InMemoryTenantRegistry();
        String keyA = ApiKeys.generate();
        String keyB = ApiKeys.generate();
        registry.register(tenant("tenant-a"), ApiKeys.hash(keyA));
        registry.register(tenant("tenant-b"), ApiKeys.hash(keyB));

        assertThat(registry.authenticate(keyA))
                .hasValueSatisfying(t -> assertThat(t.id().value()).isEqualTo("tenant-a"));
        assertThat(registry.authenticate(keyB))
                .hasValueSatisfying(t -> assertThat(t.id().value()).isEqualTo("tenant-b"));
    }

    @Test
    void unknownMalformedAndEmptyKeysAllMissTheSameWay() {
        var registry = new InMemoryTenantRegistry();
        registry.register(tenant("acme"), ApiKeys.hash(ApiKeys.generate()));

        assertThat(registry.authenticate(ApiKeys.generate())).isEmpty(); // valid shape, unknown
        assertThat(registry.authenticate("not-a-key-at-all")).isEmpty();
        assertThat(registry.authenticate("sfk_")).isEmpty();
        assertThat(registry.authenticate("")).isEmpty();
        assertThat(registry.authenticate(null)).isEmpty();
    }

    @Test
    void sameKeyHashForTwoTenantsRefusesToRegister() {
        var registry = new InMemoryTenantRegistry();
        String hash = ApiKeys.hash(ApiKeys.generate());
        registry.register(tenant("tenant-a"), hash);

        assertThatThrownBy(() -> registry.register(tenant("tenant-b"), hash))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reRegisteringATenantWithAnAdditionalKeyIsAllowed() {
        var registry = new InMemoryTenantRegistry();
        String first = ApiKeys.generate();
        String second = ApiKeys.generate();
        registry.register(tenant("acme"), ApiKeys.hash(first));
        registry.register(tenant("acme"), ApiKeys.hash(first), ApiKeys.hash(second));

        assertThat(registry.authenticate(first)).isPresent();
        assertThat(registry.authenticate(second)).isPresent();
    }
}
