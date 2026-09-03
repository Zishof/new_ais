package id.aisnext.websupport.infrastructure;

import id.aisnext.tenant.api.RouteOwner;
import id.aisnext.tenant.api.TenantContext;
import id.aisnext.tenant.api.TenantRouteDecision;
import id.aisnext.tenant.api.TenantRoutePolicy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enforces the control-plane route owner after trusted tenant resolution.
 *
 * <p>Only explicitly governed prefixes are inspected. A governed request fails closed when its
 * decision is absent or owned by the legacy application, making per-tenant rollback effective even
 * if a reverse-proxy rule is temporarily stale.</p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
public final class TenantRouteGateFilter extends OncePerRequestFilter {
    private final TenantRoutePolicy routes;
    private final List<String> governedPrefixes;

    /**
     * Creates a route gate for the migrated prefixes owned by this AIS Next deployment.
     *
     * @param routes tenant route-policy source
     * @param governedPrefixes prefixes that must have an explicit {@code NEXT} decision
     */
    public TenantRouteGateFilter(TenantRoutePolicy routes, List<String> governedPrefixes) {
        this.routes = routes;
        this.governedPrefixes = List.copyOf(governedPrefixes);
    }

    /**
     * Allows a governed request only when its most-specific decision is owned by AIS Next.
     *
     * @param request current tenant-resolved request
     * @param response response used for fail-closed errors
     * @param chain remaining filter chain
     * @throws ServletException when downstream processing fails
     * @throws IOException when the error or downstream response cannot be written
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        TenantRouteDecision decision = routes.findRoute(
                        TenantContext.require().id(), request.getRequestURI())
                .orElse(null);
        if (decision == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "No route ownership decision is available");
            return;
        }
        if (decision.owner() != RouteOwner.NEXT) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Route is owned by the legacy application");
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * Skips unmanaged paths so public and infrastructure traffic does not query route metadata.
     *
     * @param request current servlet request
     * @return {@code true} when no governed prefix contains the request path
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return governedPrefixes.stream().noneMatch(prefix ->
                path.equals(prefix) || path.startsWith(prefix + "/"));
    }
}
