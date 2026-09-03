package id.aisnext.security.api;

import id.aisnext.tenant.api.TenantId;
import java.time.Instant;
import java.util.Objects;

/**
 * Validated claims carried by the compact AIS legacy-to-Next handoff token.
 *
 * @param issuer exact identity of the trusted token producer
 * @param audience exact receiving service identifier
 * @param tenantId tenant bound to both trusted host and authentication
 * @param userId authenticated legacy user identifier
 * @param activeRoleId role selected for the new session
 * @param nonce unique one-time value used for replay prevention
 * @param expiresAt strict token expiry instant
 */
public record HandoffClaims(String issuer, String audience, TenantId tenantId, String userId,
                            String activeRoleId, String nonce, Instant expiresAt) {
    /**
     * Normalizes bounded text claims and validates all required components.
     *
     * @throws NullPointerException when a required component is {@code null}
     * @throws IllegalArgumentException when a text claim is blank or exceeds its limit
     */
    public HandoffClaims {
        issuer = required(issuer, "issuer", 128);
        audience = required(audience, "audience", 128);
        Objects.requireNonNull(tenantId, "tenantId");
        userId = required(userId, "userId", 255);
        activeRoleId = required(activeRoleId, "activeRoleId", 255);
        nonce = required(nonce, "nonce", 128);
        Objects.requireNonNull(expiresAt, "expiresAt");
    }

    /**
     * Trims and validates one bounded string claim.
     *
     * @param value raw claim value
     * @param name claim name used in safe validation errors
     * @param max maximum permitted Java-character length
     * @return trimmed valid value
     * @throws NullPointerException when the value is {@code null}
     * @throws IllegalArgumentException when the value is blank or too long
     */
    private static String required(String value, String name, int max) {
        value = Objects.requireNonNull(value, name).trim();
        if (value.isEmpty() || value.length() > max) throw new IllegalArgumentException("invalid " + name);
        return value;
    }
}
