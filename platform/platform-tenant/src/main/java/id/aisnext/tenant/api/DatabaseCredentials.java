package id.aisnext.tenant.api;

import java.util.Objects;

/**
 * Runtime-only username/password pair resolved from a secret provider.
 *
 * <p>Instances must never be logged, serialized, cached beyond pool creation, or stored in the
 * control-plane database.</p>
 *
 * @param username database login name
 * @param password database password
 */
public record DatabaseCredentials(String username, String password) {
    /**
     * Validates that both credentials are present.
     *
     * @throws NullPointerException when either component is {@code null}
     */
    public DatabaseCredentials {
        username = Objects.requireNonNull(username, "username");
        password = Objects.requireNonNull(password, "password");
    }
}
