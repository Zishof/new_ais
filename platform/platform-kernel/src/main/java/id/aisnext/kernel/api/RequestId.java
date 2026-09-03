package id.aisnext.kernel.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Validated request correlation identifier safe for response propagation and audit linking.
 *
 * @param value trimmed identifier containing between 1 and 128 characters
 */
public record RequestId(String value) {
    /**
     * Normalizes and validates an externally supplied request identifier.
     *
     * @throws NullPointerException when {@code value} is {@code null}
     * @throws IllegalArgumentException when its trimmed length is outside 1 through 128
     */
    public RequestId {
        value = Objects.requireNonNull(value, "value").trim();
        if (value.isEmpty() || value.length() > 128) {
            throw new IllegalArgumentException("request id must contain 1-128 characters");
        }
    }

    /**
     * Creates a new identifier from a random UUID.
     *
     * @return a valid request identifier
     */
    public static RequestId generate() {
        return new RequestId(UUID.randomUUID().toString());
    }
}
