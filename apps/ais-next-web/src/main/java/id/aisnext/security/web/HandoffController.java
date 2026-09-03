package id.aisnext.security.web;

import id.aisnext.identity.application.HandoffAuthorizationService;
import id.aisnext.security.api.HandoffPrincipal;
import id.aisnext.security.api.HandoffTokenService;
import id.aisnext.tenant.api.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Converts a valid, one-time legacy handoff token into an AIS Next authenticated HTTP session.
 *
 * <p>The controller never accepts a tenant from request parameters. It requires the token tenant
 * to equal the trusted-host tenant established earlier in the filter chain, then verifies that the
 * active legacy user owns the selected active role before creating a session.</p>
 */
@Controller
public class HandoffController {
    private final HandoffTokenService tokens;
    private final SecurityContextRepository repository;
    private final HandoffAuthorizationService authorization;

    /**
     * Creates the handoff endpoint.
     *
     * @param tokens token verifier and nonce consumer
     * @param repository repository used to persist the new security context
     * @param authorization legacy identity, assigned-role, and menu-capability validator
     */
    public HandoffController(HandoffTokenService tokens, SecurityContextRepository repository,
                             HandoffAuthorizationService authorization) {
        this.tokens = tokens;
        this.repository = repository;
        this.authorization = authorization;
    }

    /**
     * Verifies and consumes a handoff token, rotates the session ID, and redirects to the dashboard.
     *
     * @param token signed, short-lived, one-time handoff token
     * @param request current servlet request containing the trusted tenant context
     * @param response response used for errors, session cookie, and redirect
     * @throws IOException if the servlet container cannot send an error or redirect
     */
    @GetMapping("/auth/handoff")
    public void handoff(@RequestParam String token, HttpServletRequest request,
                        HttpServletResponse response) throws IOException {
        HandoffPrincipal principal = tokens.verifyAndConsume(token);
        if (!TenantContext.require().id().equals(principal.tenantId())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token tenant does not match host tenant");
            return;
        }
        var authorized = authorization.authorize(principal);
        if (authorized.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Legacy identity or selected role is not authorized");
            return;
        }
        var authorities = authorized.orElseThrow().authorities().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, "HANDOFF", authorities);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true);
        request.changeSessionId();
        repository.saveContext(context, request, response);
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
