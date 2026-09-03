package id.aisnext.tenant.infrastructure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import id.aisnext.tenant.api.DatabaseCredentials;
import id.aisnext.tenant.api.TenantCatalog;
import id.aisnext.tenant.api.TenantDataSourceKey;
import id.aisnext.tenant.api.TenantDatabaseDescriptor;
import id.aisnext.tenant.api.TenantSecretResolver;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;

public final class TenantDataSourceRegistry implements AutoCloseable {
    private final TenantCatalog catalog;
    private final TenantSecretResolver secrets;
    private final int maximumCachedPools;
    private final Duration idleTtl;
    private final Clock clock;
    private final Map<TenantDataSourceKey, PoolEntry> pools = new LinkedHashMap<>(16, 0.75f, true);

    public TenantDataSourceRegistry(TenantCatalog catalog, TenantSecretResolver secrets,
                                    int maximumCachedPools, Duration idleTtl) {
        this(catalog, secrets, maximumCachedPools, idleTtl, Clock.systemUTC());
    }

    TenantDataSourceRegistry(TenantCatalog catalog, TenantSecretResolver secrets,
                             int maximumCachedPools, Duration idleTtl, Clock clock) {
        if (maximumCachedPools < 1) throw new IllegalArgumentException("maximumCachedPools must be positive");
        if (idleTtl.isNegative() || idleTtl.isZero()) throw new IllegalArgumentException("idleTtl must be positive");
        this.catalog = catalog;
        this.secrets = secrets;
        this.maximumCachedPools = maximumCachedPools;
        this.idleTtl = idleTtl;
        this.clock = clock;
    }

    public synchronized DataSource dataSource(TenantDataSourceKey key) {
        evictIdleInternal();
        PoolEntry existing = pools.get(key);
        if (existing != null) {
            existing.lastAccess = clock.instant();
            return existing.dataSource;
        }
        TenantDatabaseDescriptor descriptor = catalog.findDatabase(key)
                .orElseThrow(() -> new IllegalStateException("No database descriptor for " + key));
        HikariDataSource created = createPool(descriptor);
        pools.put(key, new PoolEntry(created, clock.instant()));
        evictOverflow();
        return created;
    }

    public synchronized int cachedPoolCount() {
        return pools.size();
    }

    public synchronized int evictIdle() {
        return evictIdleInternal();
    }

    private HikariDataSource createPool(TenantDatabaseDescriptor descriptor) {
        DatabaseCredentials credentials = secrets.resolve(descriptor.credentialReference());
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(descriptor.jdbcUrl());
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());
        config.setPoolName(poolName(descriptor.key()));
        config.setMaximumPoolSize(descriptor.maximumPoolSize());
        config.setMinimumIdle(0);
        config.setConnectionTimeout(Duration.ofSeconds(5).toMillis());
        config.setIdleTimeout(Duration.ofMinutes(2).toMillis());
        config.setMaxLifetime(Duration.ofMinutes(20).toMillis());
        config.setInitializationFailTimeout(-1);
        config.setAutoCommit(false);
        config.setReadOnly(descriptor.readOnly());
        if (descriptor.readOnly()) config.setConnectionInitSql("set default_transaction_read_only = on");
        return new HikariDataSource(config);
    }

    private int evictIdleInternal() {
        Instant cutoff = clock.instant().minus(idleTtl);
        var keys = new ArrayList<TenantDataSourceKey>();
        pools.forEach((key, entry) -> { if (entry.lastAccess.isBefore(cutoff)) keys.add(key); });
        keys.forEach(this::closeAndRemove);
        return keys.size();
    }

    private void evictOverflow() {
        while (pools.size() > maximumCachedPools) closeAndRemove(pools.keySet().iterator().next());
    }

    private void closeAndRemove(TenantDataSourceKey key) {
        PoolEntry entry = pools.remove(key);
        if (entry != null) entry.dataSource.close();
    }

    private static String poolName(TenantDataSourceKey key) {
        String tenant = key.tenantId().value().replaceAll("[^a-zA-Z0-9_-]", "_");
        return "ais-" + tenant + "-" + key.databaseRole().name().toLowerCase();
    }

    @Override public synchronized void close() {
        pools.values().forEach(entry -> entry.dataSource.close());
        pools.clear();
    }

    private static final class PoolEntry {
        private final HikariDataSource dataSource;
        private Instant lastAccess;
        private PoolEntry(HikariDataSource dataSource, Instant lastAccess) {
            this.dataSource = dataSource;
            this.lastAccess = lastAccess;
        }
    }
}
