// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.gateway.core.audit;

import ai.stonefold.gateway.core.TenantId;

import java.time.Instant;
import java.util.Map;

/**
 * One append-only audit record. The audit log is the gateway's primary
 * product: every intercepted action and its decision must land here before
 * the action's result is released to the agent (write-ahead, not
 * best-effort).
 */
public record AuditEvent(
        TenantId tenant,
        String sessionId,
        Instant at,
        String kind,
        Map<String, Object> body) {
}
