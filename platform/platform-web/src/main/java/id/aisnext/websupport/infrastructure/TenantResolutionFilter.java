package id.aisnext.websupport.infrastructure;

import id.aisnext.tenant.api.ResolvedTenant;
import id.aisnext.tenant.api.TenantContext;
import id.aisnext.tenant.api.TenantResolver;
import id.aisnext.tenant.infrastructure.UnknownTenantException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Resolves the request host to a tenant and binds it for the duration of the servlet request.
 *
 * <p>The filter publishes the resolved tenant as the {@code resolvedTenant} request attribute and
 * opens a {@link TenantContext} scope before invoking downstream filters. The scope is always
 * closed, including when request processing fails. Unknown hosts receive HTTP 404.</p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public final class TenantResolutionFilter extends OncePerRequestFilter {
    private final TenantResolver resolver;

    /**
     * Creates a request filter using the trusted-host tenant resolver.
     *
     * @param resolver resolver for host-to-tenant mappings
     */
    public TenantResolutionFilter(TenantResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Resolves and binds the tenant, delegates the request, and guarantees context cleanup.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param chain remaining servlet filter chain
     * @throws ServletException when downstream servlet processing fails
     * @throws IOException when response writing or downstream I/O fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            ResolvedTenant tenant = resolver.resolveTrustedHost(request.getServerName());
            request.setAttribute("resolvedTenant", tenant);
            try (TenantContext.Scope ignored = TenantContext.open(tenant)) {
                chain.doFilter(request, response);
            }
        } catch (UnknownTenantException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown tenant host");
        }
    }

    /**
     * Allows static assets and the liveness probe to run without tenant resolution.
     *
     * @param request current HTTP request
     * @return {@code true} for exempt infrastructure paths; otherwise {@code false}
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/assets/") || path.equals("/actuator/health/liveness");
    }
}
