package ai.stonefold.gateway.tenancy;

import ai.stonefold.gateway.core.TenantId;

import java.util.Map;

/**
 * A registered tenant and its configuration surface: which policy versions
 * are active, budget limits, rate limits. Secrets (API key hashes) are held
 * by the registry, not on this object.
 */
public record Tenant(
        TenantId id,
        String displayName,
        String activePolicyRef,
        Map<String, String> limits) {
}
