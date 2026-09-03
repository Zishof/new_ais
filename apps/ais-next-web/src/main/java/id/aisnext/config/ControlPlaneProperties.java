package id.aisnext.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * External configuration for the independently owned AIS Next control database.
 *
 * <p>Credentials intentionally have no defaults and must be supplied at runtime. None of these
 * settings describes a legacy tenant database.</p>
 */
@ConfigurationProperties("ais.control")
public class ControlPlaneProperties {
    private boolean enabled = true;
    private String jdbcUrl = "jdbc:postgresql://localhost:5432/ais_next_control";
    private String username;
    private String password;
    private int maximumPoolSize = 4;

    /**
     * Creates control-plane properties with safe connection defaults and no default credentials.
     */
    public ControlPlaneProperties() {
    }

    /**
     * Reports whether persistent control-plane infrastructure should be created.
     *
     * @return whether persistent control-plane infrastructure is enabled
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Enables or disables persistent control-plane infrastructure.
     *
     * @param enabled whether to enable persistent control-plane infrastructure
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Returns the JDBC URL used only by the control-plane datasource.
     *
     * @return JDBC URL of the dedicated control database
     */
    public String getJdbcUrl() { return jdbcUrl; }

    /**
     * Sets the JDBC URL used only by the control-plane datasource.
     *
     * @param jdbcUrl JDBC URL of the dedicated control database
     */
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }

    /**
     * Returns the control-plane username supplied by runtime configuration.
     *
     * @return runtime database username, or {@code null} when not configured
     */
    public String getUsername() { return username; }

    /**
     * Sets the control-plane username supplied by runtime configuration.
     *
     * @param username runtime database username
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Returns the control-plane password supplied by runtime configuration.
     *
     * @return runtime database password, or {@code null} when not configured
     */
    public String getPassword() { return password; }

    /**
     * Sets the runtime-only control-plane password.
     *
     * @param password runtime database password; callers must not log or persist it
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Returns the upper bound for the dedicated control-plane connection pool.
     *
     * @return maximum number of physical connections in the control pool
     */
    public int getMaximumPoolSize() { return maximumPoolSize; }

    /**
     * Sets the upper bound for the dedicated control-plane connection pool.
     *
     * @param maximumPoolSize maximum number of physical connections in the control pool
     */
    public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }
}
