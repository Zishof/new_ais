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

/**
 * Lazily creates and bounds tenant-specific HikariCP connection pools.
 *
 * <p>The registry is thread-safe: all cache mutations are synchronized. Pools are created only on
 * first use, evicted after the configured idle period, and removed in least-recently-used order
 * when the cache exceeds its maximum size. Closing the registry closes every owned pool.</p>
 */
public final class TenantDataSourceRegistry implements AutoCloseable {
    private final TenantCatalog catalog;
    private final TenantSecretResolver secrets;
    private final int maximumCachedPools;
    private final Duration idleTtl;
    private final Clock clock;
    private final Map<TenantDataSourceKey, PoolEntry> pools = new LinkedHashMap<>(16, 0.75f, true);

    /**
     * Creates a registry that measures pool idle time with the UTC system clock.
     *
     * @param catalog source of tenant database connection descriptors
     * @param secrets resolver for credential references contained in descriptors
     * @param maximumCachedPools maximum number of live tenant pools retained in memory
     * @param idleTtl duration after last access before a pool becomes eligible for eviction
     * @throws IllegalArgumentException when the maximum is less than one or the TTL is not positive
     */
    public TenantDataSourceRegistry(TenantCatalog catalog, TenantSecretResolver secrets,
                                    int maximumCachedPools, Duration idleTtl) {
        this(catalog, secrets, maximumCachedPools, idleTtl, Clock.systemUTC());
    }

    /**
     * Creates a registry with an injectable clock for deterministic package-level tests.
     *
     * @param catalog source of tenant database connection descriptors
     * @param secrets resolver for descriptor credential references
     * @param maximumCachedPools maximum number of retained pools
     * @param idleTtl positive idle duration after which a pool is evicted
     * @param clock time source used for access and eviction timestamps
     * @throws IllegalArgumentException when the maximum is less than one or the TTL is not positive
     */
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

    /**
     * Returns the cached data source for a tenant key, creating it on first use.
     *
     * <p>Before lookup this method evicts expired pools; after creation it evicts least-recently-used
     * pools until the configured cache limit is restored.</p>
     *
     * @param key tenant and database-role key
     * @return open Hikari data source for the requested tenant database
     * @throws IllegalStateException when the catalog has no descriptor for {@code key}
     */
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

    /**
     * Reports the number of connection pools currently owned by the registry.
     *
     * @return number of cached pools
     */
    public synchronized int cachedPoolCount() {
        return pools.size();
    }

    /**
     * Immediately closes and removes pools whose last access is older than the configured TTL.
     *
     * @return number of pools evicted by this call
     */
    public synchronized int evictIdle() {
        return evictIdleInternal();
    }

    /**
     * Constructs a Hikari pool from non-secret catalog metadata and externally resolved credentials.
     *
     * @param descriptor tenant database connection and pool settings
     * @return newly created pool owned by this registry
     */
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

    /**
     * Removes all cached pools that are strictly older than the current idle cutoff.
     *
     * @return number of pools removed
     */
    private int evictIdleInternal() {
        Instant cutoff = clock.instant().minus(idleTtl);
        var keys = new ArrayList<TenantDataSourceKey>();
        pools.forEach((key, entry) -> { if (entry.lastAccess.isBefore(cutoff)) keys.add(key); });
        keys.forEach(this::closeAndRemove);
        return keys.size();
    }

    /**
     * Evicts least-recently-used pools until the cache is within its configured size.
     */
    private void evictOverflow() {
        while (pools.size() > maximumCachedPools) closeAndRemove(pools.keySet().iterator().next());
    }

    /**
     * Removes and closes one pool when the supplied key is currently cached.
     *
     * @param key cache key whose pool should be released
     */
    private void closeAndRemove(TenantDataSourceKey key) {
        PoolEntry entry = pools.remove(key);
        if (entry != null) entry.dataSource.close();
    }

    /**
     * Produces a stable, metrics-friendly Hikari pool name without unsafe tenant characters.
     *
     * @param key tenant database key represented by the pool
     * @return sanitized pool name
     */
    private static String poolName(TenantDataSourceKey key) {
        String tenant = key.tenantId().value().replaceAll("[^a-zA-Z0-9_-]", "_");
        return "ais-" + tenant + "-" + key.databaseRole().name().toLowerCase();
    }

    /**
     * Closes every pool and empties the registry cache.
     */
    @Override
    public synchronized void close() {
        pools.values().forEach(entry -> entry.dataSource.close());
        pools.clear();
    }

    /**
     * Mutable cache entry pairing an owned pool with its most recent access time.
     */
    private static final class PoolEntry {
        private final HikariDataSource dataSource;
        private Instant lastAccess;
        /**
         * Creates a cache entry for a newly opened pool.
         *
         * @param dataSource pool owned by the registry
         * @param lastAccess creation or most-recent-access instant
         */
        private PoolEntry(HikariDataSource dataSource, Instant lastAccess) {
            this.dataSource = dataSource;
            this.lastAccess = lastAccess;
        }
    }
}
