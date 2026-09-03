package id.aisnext.tenant.api;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable tenant identity resolved from a trusted request host.
 *
 * @param id stable routing and security identifier
 * @param displayName human-readable name safe for UI display
 * @param mode current legacy/Next coexistence mode
 * @param locale default formatting locale
 * @param zoneId default tenant time zone
 */
public record ResolvedTenant(TenantId id, String displayName, TenantMode mode, Locale locale, ZoneId zoneId) {
    /**
     * Verifies that every tenant attribute is present.
     *
     * @throws NullPointerException when any component is {@code null}
     */
    public ResolvedTenant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(zoneId, "zoneId");
    }
}
