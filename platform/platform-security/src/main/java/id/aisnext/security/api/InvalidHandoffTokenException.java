package id.aisnext.security.api;

/**
 * Non-specific authentication failure used for malformed, tampered, expired, or replayed tokens.
 */
public final class InvalidHandoffTokenException extends RuntimeException {
    /** Creates an exception whose message does not reveal which security check failed. */
    public InvalidHandoffTokenException() { super("Handoff token is invalid, expired, or already used"); }
}
