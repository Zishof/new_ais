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

@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public final class TenantResolutionFilter extends OncePerRequestFilter {
    private final TenantResolver resolver;
    public TenantResolutionFilter(TenantResolver resolver) { this.resolver = resolver; }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
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

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/assets/") || path.equals("/actuator/health/liveness");
    }
}
