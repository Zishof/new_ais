package id.aisnext.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Development bootstrap settings for the localhost tenant and its two legacy databases.
 *
 * <p>The credential reference is an environment-variable prefix, not a password. Actual
 * credentials are resolved only when a tenant pool is opened.</p>
 */
@ConfigurationProperties("ais.tenant.local")
public class LocalTenantProperties {
    private boolean bootstrap = true;
    private String tenantKey = "local";
    private String displayName = "AIS Local";
    private String coreJdbcUrl = "jdbc:postgresql://localhost:5432/ais";
    private String fileJdbcUrl = "jdbc:postgresql://localhost:5432/streaming_ais";
    private String credentialReference = "AIS_LOCAL_DB";
    private int maximumPoolSize = 4;

    /**
     * Creates local tenant properties with localhost development defaults and no embedded secrets.
     */
    public LocalTenantProperties() {
    }

    /**
     * Reports whether startup should register the configured local tenant metadata.
     *
     * @return whether localhost metadata should be upserted at application startup
     */
    public boolean isBootstrap() { return bootstrap; }

    /**
     * Enables or disables startup registration of local tenant metadata.
     *
     * @param bootstrap whether localhost metadata should be upserted at application startup
     */
    public void setBootstrap(boolean bootstrap) { this.bootstrap = bootstrap; }

    /**
     * Returns the stable machine identifier assigned to the local tenant.
     *
     * @return stable tenant key used in URLs, tokens, and routing keys
     */
    public String getTenantKey() { return tenantKey; }

    /**
     * Sets the stable machine identifier assigned to the local tenant.
     *
     * @param tenantKey stable tenant key used in URLs, tokens, and routing keys
     */
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }

    /**
     * Returns the tenant label presented in user interfaces.
     *
     * @return human-readable tenant name shown in the UI
     */
    public String getDisplayName() { return displayName; }

    /**
     * Sets the tenant label presented in user interfaces.
     *
     * @param displayName human-readable tenant name shown in the UI
     */
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    /**
     * Returns the connection location for the local tenant's legacy CORE database.
     *
     * @return JDBC URL of the legacy CORE database
     */
    public String getCoreJdbcUrl() { return coreJdbcUrl; }

    /**
     * Sets the connection location for the local tenant's legacy CORE database.
     *
     * @param coreJdbcUrl JDBC URL of the legacy CORE database
     */
    public void setCoreJdbcUrl(String coreJdbcUrl) { this.coreJdbcUrl = coreJdbcUrl; }

    /**
     * Returns the connection location for the local tenant's legacy FILE database.
     *
     * @return JDBC URL of the legacy FILE/streaming database
     */
    public String getFileJdbcUrl() { return fileJdbcUrl; }

    /**
     * Sets the connection location for the local tenant's legacy FILE database.
     *
     * @param fileJdbcUrl JDBC URL of the legacy FILE/streaming database
     */
    public void setFileJdbcUrl(String fileJdbcUrl) { this.fileJdbcUrl = fileJdbcUrl; }

    /**
     * Returns the non-secret reference used to locate runtime database credentials.
     *
     * @return environment-variable prefix used to resolve credentials lazily
     */
    public String getCredentialReference() { return credentialReference; }

    /**
     * Sets the non-secret reference used to locate runtime database credentials.
     *
     * @param credentialReference environment-variable prefix used to resolve credentials lazily
     */
    public void setCredentialReference(String credentialReference) { this.credentialReference = credentialReference; }

    /**
     * Returns the connection bound applied independently to each local tenant pool.
     *
     * @return maximum physical connections for each local tenant pool
     */
    public int getMaximumPoolSize() { return maximumPoolSize; }

    /**
     * Sets the connection bound applied independently to each local tenant pool.
     *
     * @param maximumPoolSize maximum physical connections for each local tenant pool
     */
    public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }
}
