package id.aisnext.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ais.tenant.pool")
public class TenantPoolProperties {
    private int maximumCachedPools = 32;
    private Duration idleTtl = Duration.ofMinutes(10);
    public int getMaximumCachedPools() { return maximumCachedPools; }
    public void setMaximumCachedPools(int maximumCachedPools) { this.maximumCachedPools = maximumCachedPools; }
    public Duration getIdleTtl() { return idleTtl; }
    public void setIdleTtl(Duration idleTtl) { this.idleTtl = idleTtl; }
}
