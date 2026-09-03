package id.aisnext.security.infrastructure;

import id.aisnext.security.api.NonceStore;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe process-local nonce store for tests and explicitly non-persistent development mode.
 *
 * <p>Replay state is lost on restart, so this implementation is not suitable for a production
 * cluster.</p>
 */
public final class InMemoryNonceStore implements NonceStore {
    private final Map<String, Instant> consumed = new ConcurrentHashMap<>();
    private final Clock clock;

    /** Creates a nonce store using the system UTC clock. */
    public InMemoryNonceStore() {
        this(Clock.systemUTC());
    }

    /**
     * Creates a nonce store with an injectable clock for deterministic expiry tests.
     *
     * @param clock time source used when purging expired nonce entries
     */
    public InMemoryNonceStore(Clock clock) {
        this.clock = clock;
    }

    /**
     * Atomically accepts first use after removing entries whose expiry has passed.
     *
     * @param issuer issuer namespace
     * @param nonce one-time nonce
     * @param expiresAt retention expiry for this nonce
     * @return {@code true} for first use, otherwise {@code false}
     */
    @Override public boolean consumeOnce(String issuer, String nonce, Instant expiresAt) {
        Instant now = clock.instant();
        consumed.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
        return consumed.putIfAbsent(issuer + "\u0000" + nonce, expiresAt) == null;
    }
}
