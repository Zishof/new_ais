package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.DatabaseRole;

public final class FileRoutingDataSource extends AbstractTenantRoutingDataSource {
    public FileRoutingDataSource(TenantDataSourceRegistry registry) { super(registry, DatabaseRole.FILE); }
}
