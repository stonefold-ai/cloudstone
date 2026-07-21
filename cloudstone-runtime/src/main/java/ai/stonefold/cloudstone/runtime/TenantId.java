// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.runtime;

/**
 * Opaque tenant identifier. Every request, session, policy, audit event and
 * ledger row is scoped to exactly one tenant; nothing in the gateway may
 * operate on cross-tenant state.
 */
public record TenantId(String value) {
    public TenantId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tenant id must be non-blank");
        }
    }
}
