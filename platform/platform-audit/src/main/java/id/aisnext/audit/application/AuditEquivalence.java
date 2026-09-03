package id.aisnext.audit.application;

import id.aisnext.audit.api.AuditEvent;

public final class AuditEquivalence {
    private AuditEquivalence() {}
    public static boolean equivalent(AuditEvent legacy, AuditEvent next) {
        return legacy.tenantId().equals(next.tenantId())
                && legacy.aggregateKey().equals(next.aggregateKey())
                && legacy.operation().equals(next.operation())
                && legacy.actorId().equals(next.actorId())
                && legacy.requestId().equals(next.requestId())
                && legacy.before().equals(next.before())
                && legacy.after().equals(next.after())
                && legacy.rolledBack() == next.rolledBack();
    }
}
