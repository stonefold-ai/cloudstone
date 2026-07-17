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
     * Builds the local registry from configuration. A duplicated key hash
     * across tenants makes {@link InMemoryTenantRegistry#register} throw,
     * so a mis-seeded config refuses to boot instead of silently routing
     * one tenant's traffic to another.
     *
     * With no tenants configured, seeds a "dev" tenant with a freshly
     * generated key so a first run works with zero setup. Printing the key
     * here is the one place a plaintext key may appear in output: this IS
     * its issuance ("shown once"), local profile only — request handling
     * never logs credentials. The cloud profile has no auto-seed: an empty
     * production registry correctly means nobody can authenticate.
     */
    static InMemoryTenantRegistry buildRegistry(TenantSeedProperties seeds) {
        var registry = new InMemoryTenantRegistry();
        for (TenantSeedProperties.TenantSeed seed : seeds.tenants()) {
            String displayName = seed.displayName() != null ? seed.displayName() : seed.id();
            registry.register(
                    new Tenant(new TenantId(seed.id()), displayName, seed.activePolicyRef(), Map.of()),
                    seed.keyHashes().toArray(String[]::new));
        }
        if (seeds.tenants().isEmpty()) {
            String key = ApiKeys.generate();
            registry.register(
                    new Tenant(new TenantId("dev"), "Local dev tenant (auto-seeded)", null, Map.of()),
                    ApiKeys.hash(key));
            log.warn("""

                    No tenants configured — auto-seeded local dev tenant 'dev'.
                    API key (shown once, not stored; use as 'Authorization: Bearer <key>'):

                        {}
                    """, key);
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
