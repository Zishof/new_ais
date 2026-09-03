package id.aisnext.security.infrastructure;

import id.aisnext.security.api.NonceStore;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryNonceStore implements NonceStore {
    private final Map<String, Instant> consumed = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryNonceStore() {
        this(Clock.systemUTC());
    }

    public InMemoryNonceStore(Clock clock) {
        this.clock = clock;
    }

    @Override public boolean consumeOnce(String issuer, String nonce, Instant expiresAt) {
        Instant now = clock.instant();
        consumed.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
        return consumed.putIfAbsent(issuer + "\u0000" + nonce, expiresAt) == null;
    }
}
