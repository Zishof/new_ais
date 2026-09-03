package id.aisnext.tenant.infrastructure;

public final class UnknownTenantException extends RuntimeException {
    public UnknownTenantException(String host) { super("No active tenant is mapped to host: " + host); }
}
