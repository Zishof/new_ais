package id.aisnext.files.api;

/** Lifecycle states for coordinating metadata and binary storage without XA transactions. */
public enum FileSagaState {
    /** Metadata exists but no binary write has started. */
    PENDING_FILE,
    /** A binary write attempt is in progress. */
    STORING,
    /** The stored binary passed integrity verification. */
    VERIFIED,
    /** Metadata and verified binary are visible to readers. */
    AVAILABLE,
    /** The last storage attempt failed and may be retried or orphaned. */
    FAILED,
    /** Compensation ended the workflow and the object requires cleanup or retention handling. */
    ORPHANED
}
