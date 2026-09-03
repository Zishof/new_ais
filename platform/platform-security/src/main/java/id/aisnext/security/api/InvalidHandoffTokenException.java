package id.aisnext.security.api;

public final class InvalidHandoffTokenException extends RuntimeException {
    public InvalidHandoffTokenException() { super("Handoff token is invalid, expired, or already used"); }
}
