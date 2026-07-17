package ai.stonefold.gateway.persistence;

import ai.stonefold.gateway.core.TenantId;
import ai.stonefold.gateway.core.audit.AuditEvent;
import ai.stonefold.gateway.core.audit.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Append-only audit log over JDBC. No UPDATE or DELETE statements may ever
 * appear in this class; corrections are new events referencing the old one.
 */
public final class JdbcAuditLog implements AuditLog {

    private final JdbcTemplate jdbc;

    public JdbcAuditLog(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void append(AuditEvent event) {
        // TODO: INSERT into audit_log, body as jsonb
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public List<AuditEvent> forSession(TenantId tenant, String sessionId) {
        // TODO: SELECT ... WHERE tenant_id = ? AND session_id = ? ORDER BY seq
        throw new UnsupportedOperationException("not implemented yet");
    }
}
