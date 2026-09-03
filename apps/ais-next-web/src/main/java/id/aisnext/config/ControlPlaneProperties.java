package id.aisnext.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ais.control")
public class ControlPlaneProperties {
    private boolean enabled = true;
    private String jdbcUrl = "jdbc:postgresql://localhost:5432/ais_next_control";
    private String username;
    private String password;
    private int maximumPoolSize = 4;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getMaximumPoolSize() { return maximumPoolSize; }
    public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }
}
