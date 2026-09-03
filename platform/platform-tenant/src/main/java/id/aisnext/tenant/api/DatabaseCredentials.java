package id.aisnext.tenant.api;

import java.util.Objects;

public record DatabaseCredentials(String username, String password) {
    public DatabaseCredentials {
        username = Objects.requireNonNull(username, "username");
        password = Objects.requireNonNull(password, "password");
    }
}
