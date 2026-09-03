package id.aisnext.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ais.tenant.local")
public class LocalTenantProperties {
    private boolean bootstrap = true;
    private String tenantKey = "local";
    private String displayName = "AIS Local";
    private String coreJdbcUrl = "jdbc:postgresql://localhost:5432/ais";
    private String fileJdbcUrl = "jdbc:postgresql://localhost:5432/streaming_ais";
    private String credentialReference = "AIS_LOCAL_DB";
    private int maximumPoolSize = 4;

    public boolean isBootstrap() { return bootstrap; }
    public void setBootstrap(boolean bootstrap) { this.bootstrap = bootstrap; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getCoreJdbcUrl() { return coreJdbcUrl; }
    public void setCoreJdbcUrl(String coreJdbcUrl) { this.coreJdbcUrl = coreJdbcUrl; }
    public String getFileJdbcUrl() { return fileJdbcUrl; }
    public void setFileJdbcUrl(String fileJdbcUrl) { this.fileJdbcUrl = fileJdbcUrl; }
    public String getCredentialReference() { return credentialReference; }
    public void setCredentialReference(String credentialReference) { this.credentialReference = credentialReference; }
    public int getMaximumPoolSize() { return maximumPoolSize; }
    public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }
}
