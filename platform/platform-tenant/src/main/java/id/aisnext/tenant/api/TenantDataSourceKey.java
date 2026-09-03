package id.aisnext.tenant.api;

import java.util.Objects;

public record TenantDataSourceKey(TenantId tenantId, DatabaseRole databaseRole) {
    public TenantDataSourceKey {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(databaseRole, "databaseRole");
    }
}
