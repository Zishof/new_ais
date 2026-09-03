package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.DatabaseCredentials;
import id.aisnext.tenant.api.TenantSecretResolver;
import java.util.Objects;
import java.util.function.Function;

public final class EnvironmentTenantSecretResolver implements TenantSecretResolver {
    private final Function<String, String> environment;

    public EnvironmentTenantSecretResolver(Function<String, String> environment) {
        this.environment = Objects.requireNonNull(environment);
    }

    @Override public DatabaseCredentials resolve(String credentialReference) {
        String username = environment.apply(credentialReference + "_USERNAME");
        String password = environment.apply(credentialReference + "_PASSWORD");
        if (username == null || password == null) {
            throw new IllegalStateException("Missing environment-backed database credential: " + credentialReference);
        }
        return new DatabaseCredentials(username, password);
    }
}
