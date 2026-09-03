package id.aisnext.security.api;

import id.aisnext.tenant.api.TenantId;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;

public final class HandoffTokenCli {
    private HandoffTokenCli() {}

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
