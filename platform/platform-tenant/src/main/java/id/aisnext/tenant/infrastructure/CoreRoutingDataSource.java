package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.DatabaseRole;

public final class CoreRoutingDataSource extends AbstractTenantRoutingDataSource {
    public CoreRoutingDataSource(TenantDataSourceRegistry registry) { super(registry, DatabaseRole.CORE); }
}
