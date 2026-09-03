package id.aisnext.tenant.api;

public interface TenantSecretResolver {
    DatabaseCredentials resolve(String credentialReference);
}
