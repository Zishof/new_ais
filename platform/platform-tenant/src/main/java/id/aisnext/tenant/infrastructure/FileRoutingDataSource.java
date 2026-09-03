package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.DatabaseRole;

/**
 * Routes JDBC access to each tenant's file-metadata database.
 *
 * <p>This router is separate from the core router so file migration can retain an independent
 * database boundary and connection policy.</p>
 */
public final class FileRoutingDataSource extends AbstractTenantRoutingDataSource {
    /**
     * Creates a file-database router backed by the shared tenant pool registry.
     *
     * @param registry registry that owns tenant-specific connection pools
     */
    public FileRoutingDataSource(TenantDataSourceRegistry registry) {
        super(registry, DatabaseRole.FILE);
    }
}
