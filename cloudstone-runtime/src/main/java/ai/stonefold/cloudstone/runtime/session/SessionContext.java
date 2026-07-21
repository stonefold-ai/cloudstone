// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.runtime.session;

import ai.stonefold.cloudstone.runtime.AgentId;
import ai.stonefold.cloudstone.runtime.TenantId;

import java.time.Instant;

/**
 * Immutable view of the session an intercepted action belongs to. Mutable
 * session state (budgets, counters, interlock state) lives behind
 * {@link SessionStore}; policy evaluation only ever sees this snapshot.
 */
public record SessionContext(
        TenantId tenant,
        String sessionId,
        AgentId agent,
        Instant startedAt) {
}
