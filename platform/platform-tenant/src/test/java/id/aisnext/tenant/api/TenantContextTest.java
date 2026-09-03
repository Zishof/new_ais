package id.aisnext.tenant.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.ZoneId;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TenantContextTest {
    private final ResolvedTenant tenant = new ResolvedTenant(new TenantId("tenant-a"), "Tenant A",
            TenantMode.HYBRID, Locale.forLanguageTag("id-ID"), ZoneId.of("Asia/Jakarta"));

    @Test void scopeAlwaysClearsTenant() {
        try (TenantContext.Scope ignored = TenantContext.open(tenant)) {
            assertThat(TenantContext.require()).isEqualTo(tenant);
        }
        assertThat(TenantContext.current()).isEmpty();
    }

    @Test void nestedTenantCannotLeakOrReplaceCurrentTenant() {
        try (TenantContext.Scope ignored = TenantContext.open(tenant)) {
            assertThatThrownBy(() -> TenantContext.open(new ResolvedTenant(new TenantId("tenant-b"), "Tenant B",
                    TenantMode.LEGACY, Locale.ROOT, ZoneId.of("UTC"))))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(TenantContext.current()).isEmpty();
    }
}
