package id.aisnext.files.domain;

import id.aisnext.files.api.FileSagaState;
import id.aisnext.tenant.api.TenantId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable state machine for a file/metadata saga that avoids distributed XA transactions.
 *
 * @param id globally unique saga identifier
 * @param tenantId tenant that owns both metadata and binary
 * @param aggregateKey stable owner aggregate identifier
 * @param state current lifecycle state
 * @param attempts number of storage attempts started
 * @param updatedAt time of the latest transition
 * @param failureCode safe failure category, present only in {@link FileSagaState#FAILED}
 */
public record FileSaga(UUID id, TenantId tenantId, String aggregateKey, FileSagaState state,
                       int attempts, Instant updatedAt, String failureCode) {
    /**
     * Validates required identity/state fields and prevents a negative attempt counter.
     *
     * @throws NullPointerException when a required component is {@code null}
     * @throws IllegalArgumentException when {@code attempts} is negative
     */
    public FileSaga {
        Objects.requireNonNull(id); Objects.requireNonNull(tenantId); Objects.requireNonNull(aggregateKey);
        Objects.requireNonNull(state); Objects.requireNonNull(updatedAt);
        if (attempts < 0) throw new IllegalArgumentException("attempts must be non-negative");
    }

    /**
     * Applies one allowed state transition and returns a new saga value.
     *
     * @param next requested next state
     * @param now transition timestamp
     * @param failureCode safe failure category required when entering {@code FAILED}
     * @return new immutable saga state; the current instance is unchanged
     * @throws IllegalStateException when the transition is not in the state machine
     * @throws NullPointerException when a failed transition omits its failure code
     */
    public FileSaga transition(FileSagaState next, Instant now, String failureCode) {
        if (!allowed(state, next)) throw new IllegalStateException("illegal file saga transition " + state + " -> " + next);
        int nextAttempts = next == FileSagaState.STORING ? attempts + 1 : attempts;
        return new FileSaga(id, tenantId, aggregateKey, next, nextAttempts, now,
                next == FileSagaState.FAILED ? Objects.requireNonNull(failureCode) : null);
    }

    /**
     * Evaluates the explicit state-transition table.
     *
     * @param from current state
     * @param to requested next state
     * @return whether the transition is permitted
     */
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
