package id.aisnext.tenant.api;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;

public record ResolvedTenant(TenantId id, String displayName, TenantMode mode, Locale locale, ZoneId zoneId) {
    public ResolvedTenant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(zoneId, "zoneId");
    }
}
