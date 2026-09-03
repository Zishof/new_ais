package id.aisnext.audit.api;

import java.util.List;

public interface NextAuditPort {
    void append(AuditEvent event);
    List<AuditEvent> history(String aggregateKey);
}
