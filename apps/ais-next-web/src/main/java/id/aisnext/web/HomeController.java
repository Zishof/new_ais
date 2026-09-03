package id.aisnext.web;

import id.aisnext.security.api.HandoffPrincipal;
import id.aisnext.tenant.api.ResolvedTenant;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the public entry page and authenticated tenant dashboard. */
@Controller
public class HomeController {
    /**
     * Creates the stateless controller for public and authenticated HTML pages.
     */
    public HomeController() {
    }

    /**
     * Renders the public explanation of the legacy-to-Next handoff flow.
     *
     * @return Thymeleaf template name for the landing page
     */
    @GetMapping("/") String landing() { return "index"; }

    /**
     * Renders the authenticated dashboard for the host-resolved tenant.
     *
     * @param principal authenticated user, normally a {@link HandoffPrincipal}
     * @param tenant trusted-host tenant injected from request scope
     * @param model Thymeleaf model populated for rendering
     * @return Thymeleaf template name for the dashboard
     */
    @GetMapping("/dashboard") String dashboard(Principal principal, ResolvedTenant tenant, Model model) {
        model.addAttribute("tenant", tenant);
        model.addAttribute("principal", principal instanceof HandoffPrincipal hp ? hp : principal);
        return "dashboard";
    }
}
