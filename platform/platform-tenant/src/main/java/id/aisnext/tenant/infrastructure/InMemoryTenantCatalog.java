package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.ResolvedTenant;
import id.aisnext.tenant.api.TenantCatalog;
import id.aisnext.tenant.api.TenantDataSourceKey;
import id.aisnext.tenant.api.TenantDatabaseDescriptor;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable in-memory tenant catalog intended for local bootstrap and deterministic tests.
 *
 * <p>Both input maps are defensively copied. No tenant or database registration can be changed
 * after construction.</p>
 */
public final class InMemoryTenantCatalog implements TenantCatalog {
    private final Map<String, ResolvedTenant> hosts;
    private final Map<TenantDataSourceKey, TenantDatabaseDescriptor> databases;

    /**
     * Creates a catalog from trusted-host and database-descriptor mappings.
     *
     * @param hosts normalized host name to resolved tenant mappings
     * @param databases tenant and database-role keys to connection descriptors
     * @throws NullPointerException when either map, key, or value is {@code null}
     */
    public InMemoryTenantCatalog(Map<String, ResolvedTenant> hosts,
                                 Map<TenantDataSourceKey, TenantDatabaseDescriptor> databases) {
        this.hosts = Map.copyOf(hosts);
        this.databases = Map.copyOf(databases);
    }

    /**
     * Finds a tenant by an already-normalized trusted host name.
     *
     * @param normalizedHost lowercase ASCII host without a port
     * @return matching tenant, or an empty optional when no mapping exists
     */
    @Override
    public Optional<ResolvedTenant> findByTrustedHost(String normalizedHost) {
        return Optional.ofNullable(hosts.get(normalizedHost));
    }

    /**
     * Finds a database descriptor by tenant and logical database role.
     *
     * @param key tenant database lookup key
     * @return matching descriptor, or an empty optional when none exists
     */
    @Override
    public Optional<TenantDatabaseDescriptor> findDatabase(TenantDataSourceKey key) {
        return Optional.ofNullable(databases.get(key));
    }
}
