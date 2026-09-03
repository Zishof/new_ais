package id.aisnext.tenant.api;

import java.util.Optional;

/** Read-only source of trusted tenant identity and database routing metadata. */
public interface TenantCatalog {
    /**
     * Resolves an exact normalized host from the allowlisted tenant-domain catalog.
     *
     * @param normalizedHost lowercase DNS host without port or trailing dot
     * @return tenant metadata, or empty when the host is not trusted
     */
    Optional<ResolvedTenant> findByTrustedHost(String normalizedHost);

    /**
     * Looks up one tenant/database-role descriptor without opening a connection.
     *
     * @param key tenant and logical database role
     * @return routing descriptor, or empty when not configured or disabled
     */
    Optional<TenantDatabaseDescriptor> findDatabase(TenantDataSourceKey key);
}
