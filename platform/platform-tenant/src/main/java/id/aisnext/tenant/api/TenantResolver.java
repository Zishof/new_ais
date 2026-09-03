package id.aisnext.tenant.api;

/** Security boundary for resolving tenants exclusively from trusted request hosts. */
public interface TenantResolver {
    /**
     * Resolves and validates a request host against the tenant-domain catalog.
     *
     * @param host raw server host supplied by the servlet container
     * @return trusted tenant metadata
     * @throws RuntimeException when the host is malformed or not allowlisted
     */
    ResolvedTenant resolveTrustedHost(String host);
}
