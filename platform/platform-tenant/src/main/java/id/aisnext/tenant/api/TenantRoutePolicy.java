package id.aisnext.tenant.api;

import java.util.Optional;

/** Read-only source of tenant-specific route ownership and write-policy decisions. */
@FunctionalInterface
public interface TenantRoutePolicy {
    /**
     * Finds the longest control-plane route prefix matching a tenant request path.
     *
     * @param tenantId tenant resolved from the trusted request host
     * @param requestPath normalized absolute servlet request path
     * @return matching decision, or empty when the route is not governed by this policy
     */
    Optional<TenantRouteDecision> findRoute(TenantId tenantId, String requestPath);
}
