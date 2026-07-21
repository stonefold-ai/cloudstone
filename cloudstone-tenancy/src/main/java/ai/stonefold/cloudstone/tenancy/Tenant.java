// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.tenancy;

import ai.stonefold.cloudstone.runtime.TenantId;

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
