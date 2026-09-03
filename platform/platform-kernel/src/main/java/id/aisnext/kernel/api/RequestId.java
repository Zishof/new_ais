package id.aisnext.kernel.api;

import java.util.Objects;
import java.util.UUID;

public record RequestId(String value) {
    public RequestId {
        value = Objects.requireNonNull(value, "value").trim();
        if (value.isEmpty() || value.length() > 128) {
            throw new IllegalArgumentException("request id must contain 1-128 characters");
        }
    }

    public static RequestId generate() {
        return new RequestId(UUID.randomUUID().toString());
    }
}
