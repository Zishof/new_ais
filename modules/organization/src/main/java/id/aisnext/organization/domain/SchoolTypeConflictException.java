package id.aisnext.organization.domain;

/** Raised for duplicate names, stale versions, or reverse-reference delete conflicts. */
public final class SchoolTypeConflictException extends RuntimeException {
    /**
     * Creates a conflict with an operator-safe explanation.
     *
     * @param message conflict explanation without SQL or credential details
     */
    public SchoolTypeConflictException(String message) {
        super(message);
    }
}
