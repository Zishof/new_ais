package id.aisnext.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configures the global bound and idle eviction period for lazily created tenant pools. */
@ConfigurationProperties("ais.tenant.pool")
public class TenantPoolProperties {
    private int maximumCachedPools = 32;
    private Duration idleTtl = Duration.ofMinutes(10);

    /**
     * Creates tenant pool properties with bounded cache and idle-time defaults.
     */
    public TenantPoolProperties() {
    }

    /**
     * Returns the global bound for lazily created tenant connection pools.
     *
     * @return maximum number of tenant pools cached across CORE and FILE roles
     */
    public int getMaximumCachedPools() { return maximumCachedPools; }

    /**
     * Sets the global bound for lazily created tenant connection pools.
     *
     * @param maximumCachedPools maximum number of tenant pools cached across CORE and FILE roles
     */
    public void setMaximumCachedPools(int maximumCachedPools) { this.maximumCachedPools = maximumCachedPools; }

    /**
     * Returns how long an unused tenant pool may remain cached.
     *
     * @return duration after which an unused pool becomes eligible for eviction
     */
    public Duration getIdleTtl() { return idleTtl; }

    /**
     * Sets how long an unused tenant pool may remain cached.
     *
     * @param idleTtl duration after which an unused pool becomes eligible for eviction
     */
    public void setIdleTtl(Duration idleTtl) { this.idleTtl = idleTtl; }
}
