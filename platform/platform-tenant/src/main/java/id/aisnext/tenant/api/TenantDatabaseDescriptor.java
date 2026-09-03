package id.aisnext.tenant.api;

import java.util.Objects;

public record TenantDatabaseDescriptor(
        TenantDataSourceKey key,
        String jdbcUrl,
        String credentialReference,
        int maximumPoolSize,
        boolean readOnly) {
    public TenantDatabaseDescriptor {
        Objects.requireNonNull(key, "key");
        jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl").trim();
        credentialReference = Objects.requireNonNull(credentialReference, "credentialReference").trim();
        if (!jdbcUrl.startsWith("jdbc:postgresql://")) throw new IllegalArgumentException("only PostgreSQL JDBC URLs are allowed");
        if (credentialReference.isEmpty()) throw new IllegalArgumentException("credential reference is required");
        if (maximumPoolSize < 1 || maximumPoolSize > 20) throw new IllegalArgumentException("pool size must be 1-20");
    }
}
