package id.aisnext.audit.api;

import java.util.List;

public interface LegacyAuditPort {
    List<AuditEvent> history(String aggregateKey);
}
