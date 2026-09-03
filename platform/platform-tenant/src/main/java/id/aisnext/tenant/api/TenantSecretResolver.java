package id.aisnext.tenant.api;

/** Resolves runtime database credentials from a non-secret control-plane reference. */
public interface TenantSecretResolver {
    /**
     * Resolves credentials only when a tenant pool must be created.
     *
     * @param credentialReference provider-specific reference recorded in routing metadata
     * @return runtime-only database credentials
     * @throws RuntimeException when the reference is invalid or required secret material is absent
     */
    DatabaseCredentials resolve(String credentialReference);
}
