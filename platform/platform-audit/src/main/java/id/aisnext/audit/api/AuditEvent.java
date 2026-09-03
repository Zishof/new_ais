package id.aisnext.audit.api;

import id.aisnext.kernel.api.RequestId;
import id.aisnext.tenant.api.TenantId;
import java.time.Instant;
import java.util.Map;

/**
 * Normalized audit projection used to compare legacy and AIS Next write histories.
 *
 * @param tenantId tenant that owns the audited aggregate
 * @param aggregateKey stable aggregate type and identifier
 * @param operation business operation name
 * @param actorId authenticated actor identifier
 * @param occurredAt UTC occurrence time
 * @param requestId request correlation identifier
 * @param before immutable pre-operation state, or an empty map when unavailable
 * @param after immutable post-operation state, or an empty map when unavailable
 * @param rolledBack whether the business operation was rolled back
 */
public record AuditEvent(TenantId tenantId, String aggregateKey, String operation, String actorId,
                         Instant occurredAt, RequestId requestId, Map<String, ?> before,
                         Map<String, ?> after, boolean rolledBack) {
    /** Creates an audit projection and defensively copies its before/after snapshots. */
    public AuditEvent {
        before = before == null ? Map.of() : Map.copyOf(before);
        after = after == null ? Map.of() : Map.copyOf(after);
    }
}
