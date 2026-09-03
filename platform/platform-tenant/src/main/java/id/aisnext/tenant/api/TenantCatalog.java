package id.aisnext.tenant.api;

import java.util.Optional;

public interface TenantCatalog {
    Optional<ResolvedTenant> findByTrustedHost(String normalizedHost);
    Optional<TenantDatabaseDescriptor> findDatabase(TenantDataSourceKey key);
}
