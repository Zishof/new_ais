package id.aisnext.web;

import id.aisnext.security.api.HandoffPrincipal;
import id.aisnext.tenant.api.ResolvedTenant;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the public entry page and authenticated tenant dashboard. */
@Controller
public class HomeController {
    private final String legacyLoginUrl;

    /**
     * Creates the controller with the operator-configured AIS Legacy login destination.
     *
     * @param legacyLoginUrl trusted login URL rendered on the public entry page
     */
    public HomeController(@Value("${ais.legacy.login-url}") String legacyLoginUrl) {
        this.legacyLoginUrl = legacyLoginUrl;
    }

    /**
     * Renders the public explanation of the legacy-to-Next handoff flow.
     *
     * @param model Thymeleaf model receiving the trusted Legacy login URL
     * @return Thymeleaf template name for the landing page
     */
    @GetMapping("/")
    String landing(Model model) {
        model.addAttribute("legacyLoginUrl", legacyLoginUrl);
        return "index";
    }

    /**
     * Renders the authenticated dashboard for the host-resolved tenant.
     *
     * @param principal authenticated security identity, either the {@link HandoffPrincipal}
     *                  itself or an {@link Authentication} that contains it
     * @param tenant trusted-host tenant injected from request scope
     * @param model Thymeleaf model populated for rendering
     * @return Thymeleaf template name for the dashboard
     */
    @GetMapping("/dashboard") String dashboard(Principal principal, ResolvedTenant tenant, Model model) {
        HandoffPrincipal handoffPrincipal = null;
        if (principal instanceof HandoffPrincipal directPrincipal) {
            handoffPrincipal = directPrincipal;
        } else if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof HandoffPrincipal authenticatedPrincipal) {
            handoffPrincipal = authenticatedPrincipal;
        }
        model.addAttribute("userDisplayName", handoffPrincipal == null
                ? principal.getName()
                : handoffPrincipal.getName());
        model.addAttribute("activeRoleId", handoffPrincipal == null
                ? "Peran terverifikasi"
                : handoffPrincipal.activeRoleId());
        model.addAttribute("tenantDisplayName", tenant.displayName());
        model.addAttribute("tenantMode", tenant.mode().name());
        return "dashboard";
    }
}
