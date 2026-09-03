package id.aisnext.tenant.api;

import java.util.Objects;

public record TenantId(String value) {
    public TenantId {
        value = Objects.requireNonNull(value, "value").trim().toLowerCase();
        if (!value.matches("(?:[a-z0-9]|[a-z0-9][a-z0-9_-]{0,62}[a-z0-9])")) {
            throw new IllegalArgumentException("invalid tenant id");
        }
    }

    @Override public String toString() { return value; }
}
