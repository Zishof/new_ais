package id.aisnext.tenant.infrastructure;

/**
 * Indicates that no active tenant mapping exists for a normalized request host.
 *
 * <p>Web adapters translate this exception into a not-found response without revealing catalog
 * details.</p>
 */
public final class UnknownTenantException extends RuntimeException {
    /**
     * Creates an exception identifying the host that failed tenant resolution.
     *
     * @param host normalized host or a safe placeholder such as {@code <empty>}
     */
    public UnknownTenantException(String host) {
        super("No active tenant is mapped to host: " + host);
    }
}
