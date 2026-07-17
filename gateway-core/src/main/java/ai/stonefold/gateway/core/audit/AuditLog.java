package ai.stonefold.gateway.core.audit;

import ai.stonefold.gateway.core.TenantId;

import java.util.List;

/** Append-only, tenant-scoped audit sink. */
public interface AuditLog {

    void append(AuditEvent event);

    List<AuditEvent> forSession(TenantId tenant, String sessionId);
}
