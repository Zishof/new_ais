package id.aisnext.organization.web;

/**
 * Stable error body for school-type API failures.
 *
 * @param code machine-readable failure category
 * @param message operator-safe Indonesian explanation
 */
public record SchoolTypeApiError(String code, String message) {
    /** Creates the immutable API error payload. */
    public SchoolTypeApiError {
    }
}
