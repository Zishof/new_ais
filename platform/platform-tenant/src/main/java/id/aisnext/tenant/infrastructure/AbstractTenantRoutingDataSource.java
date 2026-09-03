package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.DatabaseRole;
import id.aisnext.tenant.api.TenantContext;
import id.aisnext.tenant.api.TenantDataSourceKey;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

abstract class AbstractTenantRoutingDataSource extends AbstractRoutingDataSource {
    private final TenantDataSourceRegistry registry;
    private final DatabaseRole role;

    AbstractTenantRoutingDataSource(TenantDataSourceRegistry registry, DatabaseRole role) {
        this.registry = registry;
        this.role = role;
        setTargetDataSources(Map.of("bootstrap", new UnavailableDataSource()));
        setLenientFallback(false);
        afterPropertiesSet();
    }

    @Override protected Object determineCurrentLookupKey() {
        return new TenantDataSourceKey(TenantContext.require().id(), role);
    }

    @Override protected DataSource determineTargetDataSource() {
        return registry.dataSource((TenantDataSourceKey) determineCurrentLookupKey());
    }

    private static final class UnavailableDataSource extends AbstractDataSource {
        @Override public java.sql.Connection getConnection() throws java.sql.SQLException {
            throw new java.sql.SQLException("bootstrap datasource cannot be used");
        }
        @Override public java.sql.Connection getConnection(String u, String p) throws java.sql.SQLException {
            return getConnection();
        }
    }
}
