package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantWriteDecision;
import id.aisnext.tenant.api.TenantWritePolicy;
import java.util.Map;
import java.util.Optional;

/** Immutable development/test write policy that denies aggregates absent from its fixture map. */
public final class InMemoryTenantWritePolicy implements TenantWritePolicy {
    private final Map<TenantId, Map<String, TenantWriteDecision>> decisions;

    /**
     * Creates an immutable in-memory ownership policy.
     *
     * @param decisions decisions grouped by tenant and aggregate key
     */
    public InMemoryTenantWritePolicy(Map<TenantId, Map<String, TenantWriteDecision>> decisions) {
        this.decisions = decisions.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey, entry -> Map.copyOf(entry.getValue())));
    }

    /**
     * Finds an exact fixture decision and otherwise denies by returning empty.
     *
     * @param tenantId trusted tenant identifier
     * @param aggregateKey stable aggregate key
     * @return configured decision, or empty when absent
     */
    @Override
    public Optional<TenantWriteDecision> findWriteDecision(TenantId tenantId, String aggregateKey) {
        return Optional.ofNullable(decisions.getOrDefault(tenantId, Map.of()).get(aggregateKey));
    }
}
