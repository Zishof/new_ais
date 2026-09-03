package id.aisnext.audit.api;

import java.util.List;

/** Read-only port for normalizing legacy audit history during coexistence verification. */
public interface LegacyAuditPort {
    /**
     * Returns the legacy history for one aggregate in chronological source order.
     *
     * @param aggregateKey stable aggregate type and identifier
     * @return immutable or caller-owned list of normalized legacy audit events
     */
    List<AuditEvent> history(String aggregateKey);
}
