package id.aisnext.tenant.api;

import java.util.Objects;

/**
 * Immutable aggregate ownership decision loaded from the AIS Next control plane.
 *
 * @param aggregateKey stable aggregate identifier governed by the decision
 * @param ownership current mutually exclusive write-ownership state
 * @param version optimistic control-plane metadata version
 */
public record TenantWriteDecision(String aggregateKey, WriteOwnership ownership, long version) {
    /**
     * Validates the aggregate identifier, ownership value, and metadata version.
     *
     * @throws NullPointerException when the aggregate key or ownership is {@code null}
     * @throws IllegalArgumentException when the aggregate key is blank or version is negative
     */
    public TenantWriteDecision {
        aggregateKey = Objects.requireNonNull(aggregateKey, "aggregateKey").trim();
        ownership = Objects.requireNonNull(ownership, "ownership");
        if (aggregateKey.isEmpty()) throw new IllegalArgumentException("aggregateKey must not be blank");
        if (version < 0) throw new IllegalArgumentException("version must not be negative");
    }
}
