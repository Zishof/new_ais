package id.aisnext.security.api;

import java.time.Instant;

/** Atomic replay-prevention boundary for short-lived authentication handoff nonces. */
public interface NonceStore {
    /**
     * Records first use of an issuer/nonce pair or rejects a duplicate.
     *
     * @param issuer trusted token issuer namespace
     * @param nonce unguessable token nonce; persistent implementations should store only its hash
     * @param expiresAt time after which retained replay state may be purged
     * @return {@code true} only for the first accepted use
     */
    boolean consumeOnce(String issuer, String nonce, Instant expiresAt);
}
