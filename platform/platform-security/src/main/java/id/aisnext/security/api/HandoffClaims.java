package id.aisnext.security.api;

import id.aisnext.tenant.api.TenantId;
import java.time.Instant;
import java.util.Objects;

public record HandoffClaims(String issuer, String audience, TenantId tenantId, String userId,
                            String activeRoleId, String nonce, Instant expiresAt) {
    public HandoffClaims {
        issuer = required(issuer, "issuer", 128);
        audience = required(audience, "audience", 128);
        Objects.requireNonNull(tenantId, "tenantId");
        userId = required(userId, "userId", 255);
        activeRoleId = required(activeRoleId, "activeRoleId", 255);
        nonce = required(nonce, "nonce", 128);
        Objects.requireNonNull(expiresAt, "expiresAt");
    }

    private static String required(String value, String name, int max) {
        value = Objects.requireNonNull(value, name).trim();
        if (value.isEmpty() || value.length() > max) throw new IllegalArgumentException("invalid " + name);
        return value;
    }
}
