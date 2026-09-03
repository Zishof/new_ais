package id.aisnext.security.web;

import id.aisnext.security.api.HandoffPrincipal;
import id.aisnext.security.api.HandoffTokenService;
import id.aisnext.tenant.api.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HandoffController {
    private final HandoffTokenService tokens;
    private final SecurityContextRepository repository;

    public HandoffController(HandoffTokenService tokens, SecurityContextRepository repository) {
        this.tokens = tokens;
        this.repository = repository;
    }

    @GetMapping("/auth/handoff")
    public void handoff(@RequestParam String token, HttpServletRequest request,
                        HttpServletResponse response) throws IOException {
        HandoffPrincipal principal = tokens.verifyAndConsume(token);
        if (!TenantContext.require().id().equals(principal.tenantId())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token tenant does not match host tenant");
            return;
        }
        String authority = "ROLE_" + principal.activeRoleId().replaceAll("[^A-Za-z0-9_]", "_").toUpperCase();
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, "HANDOFF", List.of(new SimpleGrantedAuthority(authority)));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true);
        request.changeSessionId();
        repository.saveContext(context, request, response);
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
