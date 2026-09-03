package id.aisnext.tenant.api;

import java.util.Objects;

/**
 * Immutable control-plane decision for the longest matching tenant route prefix.
 *
 * @param moduleKey stable business-module key
 * @param routePrefix normalized absolute route prefix without a trailing slash
 * @param owner application currently permitted to serve the route
 * @param writeOwnership write policy associated with the routed aggregate
 * @param version optimistic-lock version from the control plane
 */
public record TenantRouteDecision(String moduleKey, String routePrefix, RouteOwner owner,
                                  WriteOwnership writeOwnership, long version) {
    /**
     * Validates the route decision before it crosses the control-plane boundary.
     *
     * @throws NullPointerException when a required value is {@code null}
     * @throws IllegalArgumentException when the module key, route prefix, or version is invalid
     */
    public TenantRouteDecision {
        moduleKey = Objects.requireNonNull(moduleKey, "moduleKey").trim();
        routePrefix = Objects.requireNonNull(routePrefix, "routePrefix").trim();
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(writeOwnership, "writeOwnership");
        if (moduleKey.isEmpty()) {
            throw new IllegalArgumentException("moduleKey must not be blank");
        }
        if (!routePrefix.startsWith("/") || routePrefix.length() > 1 && routePrefix.endsWith("/")) {
            throw new IllegalArgumentException("routePrefix must be absolute and omit a trailing slash");
        }
        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative");
        }
    }

    /**
     * Reports whether the request path belongs to this exact route prefix boundary.
     *
     * @param requestPath normalized servlet request path
     * @return {@code true} for the prefix itself or a child path separated by {@code /}
     */
    public boolean matches(String requestPath) {
        return requestPath.equals(routePrefix) || requestPath.startsWith(routePrefix + "/");
    }
}
