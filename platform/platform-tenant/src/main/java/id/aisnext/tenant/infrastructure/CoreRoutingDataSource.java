package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.DatabaseRole;

/**
 * Routes JDBC access to each tenant's core application database.
 *
 * <p>The active tenant is read from the request-bound tenant context by the base router.</p>
 */
public final class CoreRoutingDataSource extends AbstractTenantRoutingDataSource {
    /**
     * Creates a core-database router backed by the shared tenant pool registry.
     *
     * @param registry registry that owns tenant-specific connection pools
     */
    public CoreRoutingDataSource(TenantDataSourceRegistry registry) {
        super(registry, DatabaseRole.CORE);
    }
}
