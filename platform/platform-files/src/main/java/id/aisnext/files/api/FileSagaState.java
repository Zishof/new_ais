package id.aisnext.files.api;

public enum FileSagaState {
    PENDING_FILE,
    STORING,
    VERIFIED,
    AVAILABLE,
    FAILED,
    ORPHANED
}
