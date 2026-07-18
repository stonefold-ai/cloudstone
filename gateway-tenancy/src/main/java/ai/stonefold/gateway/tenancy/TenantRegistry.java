// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.tenancy;

import ai.stonefold.gateway.core.TenantId;

import java.util.Optional;

/** Source of truth for registered tenants. */
public interface TenantRegistry {

    Optional<Tenant> find(TenantId id);

    /**
     * Resolve a presented credential (API key) to its tenant. Constant-time
     * comparison against stored hashes; a miss must be indistinguishable in
     * timing from a hit on the wrong tenant.
     */
    Optional<Tenant> authenticate(String presentedApiKey);
}
