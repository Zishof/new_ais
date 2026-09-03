package id.aisnext.organization.domain;

/** Raised when a school-type command violates the audited legacy-compatible input contract. */
public final class SchoolTypeValidationException extends RuntimeException {
    /**
     * Creates a validation failure suitable for API and form presentation.
     *
     * @param message Indonesian operator-facing validation explanation
     */
    public SchoolTypeValidationException(String message) {
        super(message);
    }
}
