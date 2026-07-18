// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.core.session;

import ai.stonefold.gateway.core.TenantId;

import java.util.Optional;

/**
 * Tenant-scoped session state. In-memory for local runs; a shared store
 * (e.g. Postgres) when the gateway runs as more than one replica, so that
 * budgets and counters stay a real bound rather than a per-replica one.
 */
public interface SessionStore {

    SessionContext create(SessionContext session);

    Optional<SessionContext> find(TenantId tenant, String sessionId);

    void close(TenantId tenant, String sessionId);
}
