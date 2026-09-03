package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.DatabaseRole;
import id.aisnext.tenant.api.TenantContext;
import id.aisnext.tenant.api.TenantDataSourceKey;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Base Spring routing data source that selects a tenant-specific pool for a fixed database role.
 *
 * <p>The lookup key is derived exclusively from {@link TenantContext}; callers must therefore
 * open a tenant scope before asking this data source for a connection. The bootstrap target is a
 * deliberately unusable placeholder required by Spring initialization and is never a fallback.</p>
 */
abstract class AbstractTenantRoutingDataSource extends AbstractRoutingDataSource {
    private final TenantDataSourceRegistry registry;
    private final DatabaseRole role;

    /**
     * Creates a router for one logical tenant database role.
     *
     * @param registry registry that lazily creates and caches tenant pools
     * @param role database role selected by this router
     */
    AbstractTenantRoutingDataSource(TenantDataSourceRegistry registry, DatabaseRole role) {
        this.registry = registry;
        this.role = role;
        setTargetDataSources(Map.of("bootstrap", new UnavailableDataSource()));
        setLenientFallback(false);
        afterPropertiesSet();
    }

    /**
     * Builds the registry key from the tenant bound to the current thread and this router's role.
     *
     * @return current tenant and database-role key
     * @throws IllegalStateException when no tenant scope is active
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return new TenantDataSourceKey(TenantContext.require().id(), role);
    }

    /**
     * Resolves or creates the tenant pool associated with the current lookup key.
     *
     * @return tenant-specific data source
     * @throws IllegalStateException when the tenant database is not registered
     */
    @Override
    protected DataSource determineTargetDataSource() {
        return registry.dataSource((TenantDataSourceKey) determineCurrentLookupKey());
    }

    /**
     * Placeholder data source that prevents accidental use before a tenant is resolved.
     */
    private static final class UnavailableDataSource extends AbstractDataSource {
        /**
         * Creates the deliberately unusable Spring bootstrap target.
         */
        private UnavailableDataSource() {
        }

        /**
         * Always rejects an unscoped connection request.
         *
         * @return never returns normally
         * @throws java.sql.SQLException on every invocation because no tenant is available
         */
        @Override
        public java.sql.Connection getConnection() throws java.sql.SQLException {
            throw new java.sql.SQLException("bootstrap datasource cannot be used");
        }

        /**
         * Always rejects an unscoped connection request, ignoring supplied credentials.
         *
         * @param username ignored JDBC username
         * @param password ignored JDBC password
         * @return never returns normally
         * @throws java.sql.SQLException on every invocation because no tenant is available
         */
        @Override
        public java.sql.Connection getConnection(String username, String password) throws java.sql.SQLException {
            return getConnection();
        }
    }
}
