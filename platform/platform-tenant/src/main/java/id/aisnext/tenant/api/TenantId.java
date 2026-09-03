package id.aisnext.tenant.api;

import java.util.Objects;

/**
 * Canonical lowercase tenant identifier safe for routing keys and signed handoff claims.
 *
 * @param value 1-64 characters using lowercase ASCII letters, digits, underscore, or hyphen;
 *              the first and last character must be alphanumeric
 */
public record TenantId(String value) {
    /**
     * Trims, lowercases, and validates the identifier.
     *
     * @throws NullPointerException when {@code value} is {@code null}
     * @throws IllegalArgumentException when the normalized identifier violates the safe pattern
     */
    public TenantId {
        value = Objects.requireNonNull(value, "value").trim().toLowerCase();
        if (!value.matches("(?:[a-z0-9]|[a-z0-9][a-z0-9_-]{0,62}[a-z0-9])")) {
            throw new IllegalArgumentException("invalid tenant id");
        }
    }

    /**
     * Returns the canonical key without record decoration.
     *
     * @return canonical tenant identifier
     */
    @Override public String toString() { return value; }
}
