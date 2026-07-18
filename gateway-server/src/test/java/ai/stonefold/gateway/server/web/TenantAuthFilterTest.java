// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.server.web;

import ai.stonefold.gateway.core.TenantId;
import ai.stonefold.gateway.tenancy.ApiKeys;
import ai.stonefold.gateway.tenancy.InMemoryTenantRegistry;
import ai.stonefold.gateway.tenancy.Tenant;
import ai.stonefold.gateway.tenancy.TenantRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Black-box contract of the tenant boundary: no resolvable tenant, no
 * handler — and the rejection reveals nothing about why.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        // In-memory H2 so this context does not contend for the file-backed
        // database the context-load smoke test opens. A tenant is seeded via
        // properties so the dev auto-seed's key printout stays out of logs.
        properties = {
                "spring.datasource.url=jdbc:h2:mem:authtest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
                "stonefold.tenants[0].id=seeded",
                "stonefold.tenants[0].key-hashes[0]=BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
        })
@ActiveProfiles("local")
// Since Boot 4 the TestRestTemplate bean is opt-in, not implied by RANDOM_PORT.
@AutoConfigureTestRestTemplate
class TenantAuthFilterTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private TenantRegistry registry;

    @Test
    void missingAndUnknownCredentialsAreRejectedIdentically() {
        ResponseEntity<String> missing = post("/submit_intent", null);
        ResponseEntity<String> unknown = post("/submit_intent", ApiKeys.generate());
        ResponseEntity<String> malformed = post("/submit_intent", "not-a-key");

        assertThat(missing.getStatusCode().value()).isEqualTo(401);
        assertThat(unknown.getStatusCode().value()).isEqualTo(401);
        assertThat(malformed.getStatusCode().value()).isEqualTo(401);
        assertThat(missing.getBody()).isEqualTo(unknown.getBody()).isEqualTo(malformed.getBody());
    }

    @Test
    void validKeyResolvesTheTenantAndReachesTheHandler() {
        String key = ApiKeys.generate();
        ((InMemoryTenantRegistry) registry)
                .register(new Tenant(new TenantId("acme"), "Acme", null, Map.of()), ApiKeys.hash(key));

        // 501 is the stub handler's answer — proof the filter let us through.
        assertThat(post("/submit_intent", key).getStatusCode().value()).isEqualTo(501);
    }

    @Test
    void healthProbesAreExemptFromTenantAuth() {
        ResponseEntity<String> health = rest.getForEntity("/actuator/health", String.class);
        assertThat(health.getStatusCode().value()).isEqualTo(200);
    }

    private ResponseEntity<String> post(String path, String bearerKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearerKey != null) {
            headers.setBearerAuth(bearerKey);
        }
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>("{}", headers), String.class);
    }
}
