package id.aisnext.tenant.api;

public interface TenantResolver {
    ResolvedTenant resolveTrustedHost(String host);
}
