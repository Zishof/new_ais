package id.aisnext.audit.application;

import id.aisnext.audit.api.AuditEvent;

/** Compares normalized audit events while tolerating different physical audit implementations. */
public final class AuditEquivalence {
    /** Prevents instantiation of this stateless comparison utility. */
    private AuditEquivalence() {}

    /**
     * Compares business-significant audit fields.
     *
     * <p>Occurrence time is intentionally excluded because separate systems may record the same
     * operation at slightly different instants. The caller must enforce its agreed time tolerance
     * separately.</p>
     *
     * @param legacy normalized event read from legacy audit storage
     * @param next normalized event read from AIS Next audit storage
     * @return {@code true} when tenant, aggregate, operation, actor, request, state, and rollback match
     */
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
