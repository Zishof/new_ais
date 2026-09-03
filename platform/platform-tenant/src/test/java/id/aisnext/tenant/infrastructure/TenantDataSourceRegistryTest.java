package id.aisnext.tenant.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import id.aisnext.tenant.api.DatabaseRole;
import id.aisnext.tenant.api.TenantDataSourceKey;
import id.aisnext.tenant.api.TenantDatabaseDescriptor;
import id.aisnext.tenant.api.TenantId;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Verifies the lazy-allocation contract of the tenant connection-pool registry.
 */
class TenantDataSourceRegistryTest {
    /** Creates the lazy pool-allocation test. */
    TenantDataSourceRegistryTest() {
    }

    /**
     * Ensures catalog size alone does not create pools before a tenant database is requested.
     */
    @Test
    void oneThousandDescriptorsDoNotOpenTwoThousandPools() {
        Map<TenantDataSourceKey, TenantDatabaseDescriptor> descriptors = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            TenantId id = new TenantId("tenant-" + i);
            for (DatabaseRole role : DatabaseRole.values()) {
                TenantDataSourceKey key = new TenantDataSourceKey(id, role);
                descriptors.put(key, new TenantDatabaseDescriptor(key,
                        "jdbc:postgresql://invalid/tenant_" + i, "TEST", 2, true));
            }
        }
        InMemoryTenantCatalog catalog = new InMemoryTenantCatalog(Map.of(), descriptors);
        try (TenantDataSourceRegistry registry = new TenantDataSourceRegistry(
                catalog, ref -> new id.aisnext.tenant.api.DatabaseCredentials("nobody", "secret"),
                16, Duration.ofMinutes(5))) {
            assertThat(registry.cachedPoolCount()).isZero();
        }
    }
}
