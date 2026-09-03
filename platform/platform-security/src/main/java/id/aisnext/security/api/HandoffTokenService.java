package id.aisnext.security.api;

import id.aisnext.tenant.api.TenantId;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Issues and verifies compact HMAC-SHA256 tokens used for one-time authentication handoff.
 *
 * <p>The format is intentionally local to AIS and versioned as {@code v1}. Signature comparison is
 * constant-time, issuer/audience are exact matches, expiry is strict, and verification succeeds
 * only when the nonce store atomically accepts first use.</p>
 */
public final class HandoffTokenService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final String expectedIssuer;
    private final String expectedAudience;
    private final byte[] signingKey;
    private final NonceStore nonces;
    private final Clock clock;

    /**
     * Creates a token service with explicit trust boundaries and time source.
     *
     * @param expectedIssuer only issuer accepted for issue and verification
     * @param expectedAudience only audience accepted for issue and verification
     * @param signingKey secret HMAC key, defensively copied and at least 32 bytes
     * @param nonces atomic one-time nonce store
     * @param clock clock used for expiry decisions
     * @throws IllegalArgumentException when the signing key is missing or shorter than 256 bits
     */
    public HandoffTokenService(String expectedIssuer, String expectedAudience, byte[] signingKey,
                               NonceStore nonces, Clock clock) {
        if (signingKey == null || signingKey.length < 32) throw new IllegalArgumentException("handoff signing key must be at least 256 bits");
        this.expectedIssuer = expectedIssuer;
        this.expectedAudience = expectedAudience;
        this.signingKey = signingKey.clone();
        this.nonces = nonces;
        this.clock = clock;
    }

    /**
     * Serializes and signs validated claims.
     *
     * @param claims handoff claims whose issuer and audience must match this service
     * @return URL-safe compact token without Base64 padding
     * @throws IllegalArgumentException when issuer or audience does not match configuration
     */
    public String issue(HandoffClaims claims) {
        if (!expectedIssuer.equals(claims.issuer()) || !expectedAudience.equals(claims.audience())) {
            throw new IllegalArgumentException("unexpected issuer or audience");
        }
        String payload = String.join(".", "v1", encode(claims.issuer()), encode(claims.audience()),
                encode(claims.tenantId().value()), encode(claims.userId()), encode(claims.activeRoleId()),
                encode(claims.nonce()), Long.toString(claims.expiresAt().getEpochSecond()));
        return payload + "." + ENCODER.encodeToString(sign(payload));
    }

    /**
     * Validates a token and atomically consumes its nonce before returning a principal.
     *
     * <p>All parsing and validation failures intentionally collapse to the same exception so a
     * client cannot distinguish tampering, expiry, or replay.</p>
     *
     * @param token untrusted compact token received from the browser
     * @return authenticated tenant/user/role principal after successful nonce consumption
     * @throws InvalidHandoffTokenException when any structural, signature, claim, expiry, or replay check fails
     */
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

    /**
     * Computes the HMAC-SHA256 signature of a serialized payload.
     *
     * @param payload exact UTF-8 payload preceding the signature segment
     * @return 32-byte HMAC digest
     * @throws IllegalStateException when the required cryptographic implementation is unavailable
     */
    private byte[] sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey, "HmacSHA256"));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", exception);
        }
    }

    /**
     * Encodes one claim as unpadded URL-safe Base64.
     *
     * @param value UTF-8 claim text
     * @return URL-safe encoded claim
     */
    private static String encode(String value) { return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8)); }

    /**
     * Decodes one unpadded URL-safe Base64 claim as UTF-8.
     *
     * @param value encoded claim
     * @return decoded claim text
     * @throws IllegalArgumentException when the input is not valid URL-safe Base64
     */
    private static String decode(String value) { return new String(DECODER.decode(value), StandardCharsets.UTF_8); }

    /**
     * Creates the deliberately non-specific public token failure.
     *
     * @return a new invalid-token exception
     */
    private static InvalidHandoffTokenException invalid() { return new InvalidHandoffTokenException(); }
}
