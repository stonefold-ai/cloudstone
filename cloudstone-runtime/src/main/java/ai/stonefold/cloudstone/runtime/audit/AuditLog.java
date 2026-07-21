// SPDX-License-Identifier: Apache-2.0
package ai.stonefold.cloudstone.runtime.audit;

import ai.stonefold.cloudstone.runtime.TenantId;

import java.util.List;

/** Append-only, tenant-scoped audit sink. */
public interface AuditLog {

    void append(AuditEvent event);

    List<AuditEvent> forSession(TenantId tenant, String sessionId);
}
