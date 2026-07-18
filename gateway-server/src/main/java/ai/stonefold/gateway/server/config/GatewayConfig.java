// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.server.config;

import ai.stonefold.gateway.core.TenantId;
import ai.stonefold.gateway.server.web.TenantAuthFilter;
import ai.stonefold.gateway.tenancy.ApiKeys;
import ai.stonefold.gateway.tenancy.InMemoryTenantRegistry;
import ai.stonefold.gateway.tenancy.Tenant;
import ai.stonefold.gateway.tenancy.TenantRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Central wiring. Profile "local" runs self-contained (H2, config-seeded
 * tenants); profile "cloud" expects Postgres and env-provided secrets.
 */
@Configuration
@EnableConfigurationProperties(TenantSeedProperties.class)
public class GatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);

    @Bean
    @Profile("local")
    TenantRegistry localTenantRegistry(TenantSeedProperties seeds) {
        return buildRegistry(seeds);
    }

    // TODO @Profile("cloud"): JdbcTenantRegistry over the tenant table

    /**
     * Builds the local registry from configuration. A mis-seeded config
     * refuses to boot instead of degrading silently: duplicate tenant ids,
     * duplicate key hashes across tenants, a tenant without keys, and
     * malformed hash values (including a plaintext key pasted where the
     * hash belongs) are all startup errors, because each of them would
     * otherwise surface only as inexplicable 401s — or worse, as one
     * tenant's credentials operating under another's definition.
     *
     * With no tenants configured, seeds a "dev" tenant with a freshly
     * generated key so a first run works with zero setup. Printing the key
     * here is the one place a plaintext key may appear in output: this IS
     * its issuance ("shown once"), local profile only — request handling
     * never logs credentials. The cloud profile has no auto-seed path.
     *
     * An empty binding can also mean the whole stonefold.tenants block is
     * misspelled or misplaced — indistinguishable from a fresh checkout at
     * this level. The seeded-tenants log line below is what makes that
     * mistake visible: if you configured tenants and see 'dev' instead of
     * them, the config is not where Spring reads it.
     */
    static InMemoryTenantRegistry buildRegistry(TenantSeedProperties seeds) {
        var registry = new InMemoryTenantRegistry();
        var ids = new LinkedHashSet<String>();
        for (TenantSeedProperties.TenantSeed seed : seeds.tenants()) {
            if (!ids.add(seed.id())) {
                throw new IllegalStateException(
                        "duplicate tenant id '" + seed.id() + "' in stonefold.tenants");
            }
            if (seed.keyHashes().isEmpty()) {
                throw new IllegalStateException(
                        "tenant '" + seed.id() + "' has no key-hashes; a tenant that can never"
                        + " authenticate is a config error, not a valid state");
            }
            for (String hash : seed.keyHashes()) {
                if (hash != null && hash.startsWith(ApiKeys.PREFIX)) {
                    throw new IllegalStateException(
                            "tenant '" + seed.id() + "' has a plaintext API key in key-hashes;"
                            + " config must carry the HASH (as printed by GenerateApiKey) —"
                            + " remove the key from the file and rotate it");
                }
                if (!ApiKeys.isHashShaped(hash)) {
                    throw new IllegalStateException(
                            "tenant '" + seed.id() + "' has a malformed key hash (expected 43"
                            + " base64url chars as printed by GenerateApiKey)");
                }
            }
            registry.register(
                    new Tenant(new TenantId(seed.id()), seed.displayName(), seed.activePolicyRef(), Map.of()),
                    seed.keyHashes().toArray(String[]::new));
        }
        if (seeds.tenants().isEmpty()) {
            String key = ApiKeys.generate();
            registry.register(
                    new Tenant(new TenantId("dev"), "Local dev tenant (auto-seeded)", null, Map.of()),
                    ApiKeys.hash(key));
            log.warn("""

                    No tenants configured — auto-seeded local dev tenant 'dev'.
                    If you DID configure tenants, your stonefold.tenants block is not where
                    Spring reads it — fix the config instead of using this key.
                    API key (shown once, not stored; use as 'Authorization: Bearer <key>'):

                        {}
                    """, key);
        } else {
            log.info("Tenant registry seeded from config: {}", ids);
        }
        return registry;
    }

    @Bean
    FilterRegistrationBean<TenantAuthFilter> tenantAuthFilter(TenantRegistry registry) {
        var registration = new FilterRegistrationBean<>(new TenantAuthFilter(registry));
        registration.setOrder(0);
        return registration;
    }
}
