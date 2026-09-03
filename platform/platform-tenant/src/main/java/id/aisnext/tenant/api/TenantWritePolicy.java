package id.aisnext.tenant.api;

import java.util.Optional;

/**
 * Fail-closed control-plane boundary for tenant-specific aggregate write ownership.
 *
 * <p>Business command handlers must call {@link #requireNextWrite(TenantId, String)} immediately
 * before opening a write transaction. A missing decision is denial, never an implicit default.</p>
 */
public interface TenantWritePolicy {
    /**
     * Finds the current ownership decision for one tenant aggregate.
     *
     * @param tenantId trusted tenant identifier established for the request
     * @param aggregateKey stable aggregate key
     * @return current decision, or empty when no ownership metadata exists
     */
    Optional<TenantWriteDecision> findWriteDecision(TenantId tenantId, String aggregateKey);

    /**
     * Requires AIS Next to be the aggregate's sole approved writer.
     *
     * @param tenantId trusted tenant identifier established for the request
     * @param aggregateKey stable aggregate key
     * @return the approved {@code NEXT_WRITE} decision
     * @throws WriteOwnershipDeniedException when metadata is missing or not {@code NEXT_WRITE}
     */
    default TenantWriteDecision requireNextWrite(TenantId tenantId, String aggregateKey) {
        TenantWriteDecision decision = findWriteDecision(tenantId, aggregateKey)
                .orElseThrow(() -> new WriteOwnershipDeniedException(
                        "No write-ownership decision exists for " + tenantId + "/" + aggregateKey));
        if (decision.ownership() != WriteOwnership.NEXT_WRITE) {
            throw new WriteOwnershipDeniedException(
                    "AIS Next is not the writer for " + tenantId + "/" + aggregateKey);
        }
        return decision;
    }
}
