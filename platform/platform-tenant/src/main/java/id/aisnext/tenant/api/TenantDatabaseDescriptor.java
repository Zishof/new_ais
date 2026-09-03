package id.aisnext.tenant.api;

import java.util.Objects;

/**
 * Non-secret metadata required to create one tenant datasource lazily.
 *
 * @param key tenant and CORE/FILE role
 * @param jdbcUrl allowlisted PostgreSQL JDBC URL
 * @param credentialReference secret-provider reference, never a credential value
 * @param maximumPoolSize per-pool physical connection limit from 1 through 20
 * @param readOnly whether Hikari and JDBC connections must enforce read-only mode
 */
public record TenantDatabaseDescriptor(
        TenantDataSourceKey key,
        String jdbcUrl,
        String credentialReference,
        int maximumPoolSize,
        boolean readOnly) {
    /**
     * Validates routing metadata before it can reach HikariCP.
     *
     * @throws NullPointerException when a required component is {@code null}
     * @throws IllegalArgumentException for non-PostgreSQL URLs, blank secret references, or unsafe pool sizes
     */
    public TenantDatabaseDescriptor {
        Objects.requireNonNull(key, "key");
        jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl").trim();
        credentialReference = Objects.requireNonNull(credentialReference, "credentialReference").trim();
        if (!jdbcUrl.startsWith("jdbc:postgresql://")) throw new IllegalArgumentException("only PostgreSQL JDBC URLs are allowed");
        if (credentialReference.isEmpty()) throw new IllegalArgumentException("credential reference is required");
        if (maximumPoolSize < 1 || maximumPoolSize > 20) throw new IllegalArgumentException("pool size must be 1-20");
    }
}
