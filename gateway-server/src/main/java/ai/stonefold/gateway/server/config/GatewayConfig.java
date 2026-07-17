package ai.stonefold.gateway.server.config;

import ai.stonefold.gateway.server.web.TenantAuthFilter;
import ai.stonefold.gateway.tenancy.InMemoryTenantRegistry;
import ai.stonefold.gateway.tenancy.TenantRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Central wiring. Profile "local" runs self-contained (H2, config-seeded
 * tenants); profile "cloud" expects Postgres and env-provided secrets.
 */
@Configuration
public class GatewayConfig {

    @Bean
    @Profile("local")
    TenantRegistry localTenantRegistry() {
        // TODO: seed from stonefold.tenants.* application properties
        return new InMemoryTenantRegistry();
    }

    // TODO @Profile("cloud"): JdbcTenantRegistry over the tenant table

    @Bean
    FilterRegistrationBean<TenantAuthFilter> tenantAuthFilter(TenantRegistry registry) {
        var registration = new FilterRegistrationBean<>(new TenantAuthFilter(registry));
        registration.setOrder(0);
        return registration;
    }
}
