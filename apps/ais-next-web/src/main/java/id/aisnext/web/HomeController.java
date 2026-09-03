package id.aisnext.web;

import id.aisnext.security.api.HandoffPrincipal;
import id.aisnext.tenant.api.ResolvedTenant;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/") String landing() { return "index"; }

    @GetMapping("/dashboard") String dashboard(Principal principal, ResolvedTenant tenant, Model model) {
        model.addAttribute("tenant", tenant);
        model.addAttribute("principal", principal instanceof HandoffPrincipal hp ? hp : principal);
        return "dashboard";
    }
}
