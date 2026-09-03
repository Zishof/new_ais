package id.aisnext.audit.api;

import id.aisnext.kernel.api.RequestId;
import id.aisnext.tenant.api.TenantId;
import java.time.Instant;
import java.util.Map;

public record AuditEvent(TenantId tenantId, String aggregateKey, String operation, String actorId,
                         Instant occurredAt, RequestId requestId, Map<String, ?> before,
                         Map<String, ?> after, boolean rolledBack) {
    public AuditEvent {
        before = before == null ? Map.of() : Map.copyOf(before);
        after = after == null ? Map.of() : Map.copyOf(after);
    }
}
