package id.aisnext.tenant.api;

import java.util.Objects;

/**
 * Composite lookup key that prevents ambiguity between a tenant's CORE and FILE pools.
 *
 * @param tenantId stable tenant identifier
 * @param databaseRole logical database role
 */
public record TenantDataSourceKey(TenantId tenantId, DatabaseRole databaseRole) {
    /**
     * Verifies that both key dimensions are present.
     *
     * @throws NullPointerException when either component is {@code null}
     */
    public TenantDataSourceKey {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(databaseRole, "databaseRole");
    }
}
