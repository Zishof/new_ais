package id.aisnext.security.api;

import id.aisnext.tenant.api.TenantId;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HandoffTokenService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final String expectedIssuer;
    private final String expectedAudience;
    private final byte[] signingKey;
    private final NonceStore nonces;
    private final Clock clock;

    public HandoffTokenService(String expectedIssuer, String expectedAudience, byte[] signingKey,
                               NonceStore nonces, Clock clock) {
        if (signingKey == null || signingKey.length < 32) throw new IllegalArgumentException("handoff signing key must be at least 256 bits");
        this.expectedIssuer = expectedIssuer;
        this.expectedAudience = expectedAudience;
        this.signingKey = signingKey.clone();
        this.nonces = nonces;
        this.clock = clock;
    }

    public String issue(HandoffClaims claims) {
        if (!expectedIssuer.equals(claims.issuer()) || !expectedAudience.equals(claims.audience())) {
            throw new IllegalArgumentException("unexpected issuer or audience");
        }
        String payload = String.join(".", "v1", encode(claims.issuer()), encode(claims.audience()),
                encode(claims.tenantId().value()), encode(claims.userId()), encode(claims.activeRoleId()),
                encode(claims.nonce()), Long.toString(claims.expiresAt().getEpochSecond()));
        return payload + "." + ENCODER.encodeToString(sign(payload));
    }

    public HandoffPrincipal verifyAndConsume(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 9 || !"v1".equals(parts[0])) throw invalid();
            String payload = String.join(".", java.util.Arrays.copyOf(parts, 8));
            if (!MessageDigest.isEqual(sign(payload), DECODER.decode(parts[8]))) throw invalid();
            HandoffClaims claims = new HandoffClaims(decode(parts[1]), decode(parts[2]),
                    new TenantId(decode(parts[3])), decode(parts[4]), decode(parts[5]), decode(parts[6]),
                    Instant.ofEpochSecond(Long.parseLong(parts[7])));
            if (!expectedIssuer.equals(claims.issuer()) || !expectedAudience.equals(claims.audience())) throw invalid();
            if (!claims.expiresAt().isAfter(clock.instant())) throw invalid();
            if (!nonces.consumeOnce(claims.issuer(), claims.nonce(), claims.expiresAt())) throw invalid();
            return new HandoffPrincipal(claims.tenantId(), claims.userId(), claims.activeRoleId());
        } catch (InvalidHandoffTokenException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw invalid();
        }
    }

    private byte[] sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey, "HmacSHA256"));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", exception);
        }
    }

    private static String encode(String value) { return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8)); }
    private static String decode(String value) { return new String(DECODER.decode(value), StandardCharsets.UTF_8); }
    private static InvalidHandoffTokenException invalid() { return new InvalidHandoffTokenException(); }
}
