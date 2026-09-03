package id.aisnext.files.domain;

import id.aisnext.files.api.FileSagaState;
import id.aisnext.tenant.api.TenantId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record FileSaga(UUID id, TenantId tenantId, String aggregateKey, FileSagaState state,
                       int attempts, Instant updatedAt, String failureCode) {
    public FileSaga {
        Objects.requireNonNull(id); Objects.requireNonNull(tenantId); Objects.requireNonNull(aggregateKey);
        Objects.requireNonNull(state); Objects.requireNonNull(updatedAt);
        if (attempts < 0) throw new IllegalArgumentException("attempts must be non-negative");
    }

    public FileSaga transition(FileSagaState next, Instant now, String failureCode) {
        if (!allowed(state, next)) throw new IllegalStateException("illegal file saga transition " + state + " -> " + next);
        int nextAttempts = next == FileSagaState.STORING ? attempts + 1 : attempts;
        return new FileSaga(id, tenantId, aggregateKey, next, nextAttempts, now,
                next == FileSagaState.FAILED ? Objects.requireNonNull(failureCode) : null);
    }

    private static boolean allowed(FileSagaState from, FileSagaState to) {
        return switch (from) {
            case PENDING_FILE -> to == FileSagaState.STORING || to == FileSagaState.ORPHANED;
            case STORING -> to == FileSagaState.VERIFIED || to == FileSagaState.FAILED;
            case VERIFIED -> to == FileSagaState.AVAILABLE || to == FileSagaState.ORPHANED;
            case FAILED -> to == FileSagaState.STORING || to == FileSagaState.ORPHANED;
            case AVAILABLE, ORPHANED -> false;
        };
    }
}
