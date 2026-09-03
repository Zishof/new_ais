package id.aisnext.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configures the signed, one-time authentication handoff from AIS legacy to AIS Next.
 *
 * <p>The signing key is secret runtime material and must never be stored in source control. Issuer
 * and audience defaults are intentionally specific so tokens for another service are rejected.</p>
 */
@ConfigurationProperties("ais.security.handoff")
public class HandoffProperties {
    private String issuer = "ais-legacy";
    private String audience = "ais-next";
    private String signingKey;

    /**
     * Creates handoff properties with fixed service identity defaults and no default secret.
     */
    public HandoffProperties() {
    }

    /**
     * Returns the only token issuer accepted by AIS Next.
     *
     * @return exact issuer accepted from the legacy handoff producer
     */
    public String getIssuer() { return issuer; }

    /**
     * Sets the exact legacy issuer expected during token verification.
     *
     * @param issuer exact issuer accepted from the legacy handoff producer
     */
    public void setIssuer(String issuer) { this.issuer = issuer; }

    /**
     * Returns the audience value that identifies AIS Next.
     *
     * @return exact audience that identifies AIS Next
     */
    public String getAudience() { return audience; }

    /**
     * Sets the audience value required during token verification.
     *
     * @param audience exact audience that identifies AIS Next
     */
    public void setAudience(String audience) { this.audience = audience; }

    /**
     * Returns the runtime-only HMAC secret used to verify handoff tokens.
     *
     * @return HMAC signing key, or {@code null} when runtime configuration is incomplete
     */
    public String getSigningKey() { return signingKey; }

    /**
     * Sets the runtime-only HMAC secret used to verify handoff tokens.
     *
     * @param signingKey secret HMAC signing key; callers must not log or persist it
     */
    public void setSigningKey(String signingKey) { this.signingKey = signingKey; }
}
