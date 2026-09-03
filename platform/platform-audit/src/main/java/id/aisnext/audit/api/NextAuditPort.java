package id.aisnext.audit.api;

import java.util.List;

/** Write/read boundary for the independent AIS Next audit store. */
public interface NextAuditPort {
    /**
     * Appends one immutable audit event to the AIS Next audit history.
     *
     * @param event fully populated event for the current tenant and request
     */
    void append(AuditEvent event);

    /**
     * Returns AIS Next history for one aggregate in chronological source order.
     *
     * @param aggregateKey stable aggregate type and identifier
     * @return immutable or caller-owned list of AIS Next audit events
     */
    List<AuditEvent> history(String aggregateKey);
}
