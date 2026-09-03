package id.aisnext.security.api;

import java.time.Instant;

public interface NonceStore {
    boolean consumeOnce(String issuer, String nonce, Instant expiresAt);
}
