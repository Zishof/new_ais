package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.ResolvedTenant;
import id.aisnext.tenant.api.TenantCatalog;
import id.aisnext.tenant.api.TenantDataSourceKey;
import id.aisnext.tenant.api.TenantDatabaseDescriptor;
import java.util.Map;
import java.util.Optional;

public final class InMemoryTenantCatalog implements TenantCatalog {
    private final Map<String, ResolvedTenant> hosts;
    private final Map<TenantDataSourceKey, TenantDatabaseDescriptor> databases;

    public InMemoryTenantCatalog(Map<String, ResolvedTenant> hosts,
                                 Map<TenantDataSourceKey, TenantDatabaseDescriptor> databases) {
        this.hosts = Map.copyOf(hosts);
        this.databases = Map.copyOf(databases);
    }

    @Override public Optional<ResolvedTenant> findByTrustedHost(String normalizedHost) {
        return Optional.ofNullable(hosts.get(normalizedHost));
    }

    @Override public Optional<TenantDatabaseDescriptor> findDatabase(TenantDataSourceKey key) {
        return Optional.ofNullable(databases.get(key));
    }
}
