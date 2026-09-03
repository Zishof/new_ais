package id.aisnext.tenant.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.ZoneId;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Verifies that thread-bound tenant scopes are isolated and always cleaned up.
 */
class TenantContextTest {
    private final ResolvedTenant tenant = new ResolvedTenant(new TenantId("tenant-a"), "Tenant A",
            TenantMode.HYBRID, Locale.forLanguageTag("id-ID"), ZoneId.of("Asia/Jakarta"));

    /** Creates the tenant-context test fixture. */
    TenantContextTest() {
    }

    /**
     * Confirms that closing a scope removes the tenant from the current thread.
     */
    @Test
    void scopeAlwaysClearsTenant() {
        try (TenantContext.Scope ignored = TenantContext.open(tenant)) {
            assertThat(TenantContext.require()).isEqualTo(tenant);
        }
        assertThat(TenantContext.current()).isEmpty();
    }

    /**
     * Confirms that a nested scope cannot replace the tenant already bound to the thread.
     */
    @Test
    void nestedTenantCannotLeakOrReplaceCurrentTenant() {
        try (TenantContext.Scope ignored = TenantContext.open(tenant)) {
            assertThatThrownBy(() -> TenantContext.open(new ResolvedTenant(new TenantId("tenant-b"), "Tenant B",
                    TenantMode.LEGACY, Locale.ROOT, ZoneId.of("UTC"))))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(TenantContext.current()).isEmpty();
    }
}
