package id.aisnext.security.api;

import id.aisnext.tenant.api.TenantId;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;

/**
 * Operator-only command-line helper for issuing a short-lived local handoff token.
 *
 * <p>The command reads the signing key from {@code AIS_HANDOFF_SIGNING_KEY} and writes the token to
 * standard output. Output must be treated as a credential and must not be committed or logged.</p>
 */
public final class HandoffTokenCli {
    /** Prevents instantiation of this command-line utility. */
    private HandoffTokenCli() {}

    /**
     * Issues a 60-second token for manual smoke testing.
     *
     * @param args issuer, audience, tenant key, user ID, and active role ID in that order
     * @throws IllegalArgumentException when exactly five arguments are not supplied
     * @throws IllegalStateException when the signing-key environment variable is absent
     */
    public static void main(String[] args) {
        if (args.length != 5) {
            throw new IllegalArgumentException("Usage: <issuer> <audience> <tenant> <user> <role>");
        }
        String key = System.getenv("AIS_HANDOFF_SIGNING_KEY");
        if (key == null) throw new IllegalStateException("AIS_HANDOFF_SIGNING_KEY is required");
        HandoffTokenService service = new HandoffTokenService(args[0], args[1],
                key.getBytes(StandardCharsets.UTF_8), (issuer, nonce, expiry) -> true, Clock.systemUTC());
        HandoffClaims claims = new HandoffClaims(args[0], args[1], new TenantId(args[2]), args[3], args[4],
                java.util.UUID.randomUUID().toString(), Instant.now().plusSeconds(60));
        System.out.println(service.issue(claims));
    }
}
