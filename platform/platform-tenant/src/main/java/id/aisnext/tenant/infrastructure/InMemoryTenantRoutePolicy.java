package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantRouteDecision;
import id.aisnext.tenant.api.TenantRoutePolicy;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Immutable route policy used by isolated development and deterministic unit tests. */
public final class InMemoryTenantRoutePolicy implements TenantRoutePolicy {
    private final List<TenantRouteDecision> routes;

    /**
     * Creates a route policy from decisions that apply to the single in-memory tenant.
     *
     * @param routes route decisions copied and sorted by descending prefix length
     * @throws NullPointerException when the list or one of its decisions is {@code null}
     */
    public InMemoryTenantRoutePolicy(List<TenantRouteDecision> routes) {
        this.routes = routes.stream()
                .sorted(Comparator.comparingInt((TenantRouteDecision route) -> route.routePrefix().length())
                        .reversed())
                .toList();
    }

    /**
     * Finds the longest route prefix matching the request path.
     *
     * @param tenantId resolved tenant; retained for parity with persistent implementations
     * @param requestPath normalized absolute servlet request path
     * @return most-specific matching route decision, or empty when unmanaged
     */
    @Override
    public Optional<TenantRouteDecision> findRoute(TenantId tenantId, String requestPath) {
        return routes.stream().filter(route -> route.matches(requestPath)).findFirst();
    }
}
