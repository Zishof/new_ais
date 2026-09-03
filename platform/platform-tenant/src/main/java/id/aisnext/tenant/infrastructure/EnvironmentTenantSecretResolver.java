package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.DatabaseCredentials;
import id.aisnext.tenant.api.TenantSecretResolver;
import java.util.Objects;
import java.util.function.Function;

/**
 * Resolves tenant database credentials from environment-style key/value entries.
 *
 * <p>For a reference such as {@code TENANT_A_CORE}, this resolver reads
 * {@code TENANT_A_CORE_USERNAME} and {@code TENANT_A_CORE_PASSWORD}. It returns credentials only;
 * it does not log, cache, or persist secret values.</p>
 */
public final class EnvironmentTenantSecretResolver implements TenantSecretResolver {
    private final Function<String, String> environment;

    /**
     * Creates a resolver using the supplied environment lookup function.
     *
     * @param environment function that returns a value for an environment key, or {@code null}
     *                    when the key is absent
     * @throws NullPointerException when {@code environment} is {@code null}
     */
    public EnvironmentTenantSecretResolver(Function<String, String> environment) {
        this.environment = Objects.requireNonNull(environment);
    }

    /**
     * Resolves the username and password associated with a credential reference.
     *
     * @param credentialReference non-secret prefix used to derive the environment keys
     * @return resolved database credentials
     * @throws IllegalStateException when either required environment entry is absent
     */
    @Override
    public DatabaseCredentials resolve(String credentialReference) {
        String username = environment.apply(credentialReference + "_USERNAME");
        String password = environment.apply(credentialReference + "_PASSWORD");
        if (username == null || password == null) {
            throw new IllegalStateException("Missing environment-backed database credential: " + credentialReference);
        }
        return new DatabaseCredentials(username, password);
    }
}
