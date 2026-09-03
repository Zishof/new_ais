package id.aisnext.files.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.aisnext.files.api.FileSagaState;
import id.aisnext.tenant.api.TenantId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Unit tests for retry and terminal-state behavior of the file saga. */
class FileSagaTest {
    /** Creates the file-saga domain test. */
    FileSagaTest() {
    }

    /** Proves retry counting without XA and rejects transitions out of a terminal state. */
    @Test void supportsRetryWithoutXaAndRejectsTerminalTransitions() {
        Instant now = Instant.parse("2026-09-03T00:00:00Z");
        FileSaga saga = new FileSaga(UUID.randomUUID(), new TenantId("tenant-a"), "document:1",
                FileSagaState.PENDING_FILE, 0, now, null);
        saga = saga.transition(FileSagaState.STORING, now, null)
                .transition(FileSagaState.FAILED, now, "CHECKSUM")
                .transition(FileSagaState.STORING, now, null)
                .transition(FileSagaState.VERIFIED, now, null)
                .transition(FileSagaState.AVAILABLE, now, null);
        assertThat(saga.attempts()).isEqualTo(2);
        FileSaga terminal = saga;
        assertThatThrownBy(() -> terminal.transition(FileSagaState.STORING, now, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
