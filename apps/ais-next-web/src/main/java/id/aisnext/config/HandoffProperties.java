package id.aisnext.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ais.security.handoff")
public class HandoffProperties {
    private String issuer = "ais-legacy";
    private String audience = "ais-next";
    private String signingKey;
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getSigningKey() { return signingKey; }
    public void setSigningKey(String signingKey) { this.signingKey = signingKey; }
}
