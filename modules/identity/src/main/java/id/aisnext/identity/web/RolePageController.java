package id.aisnext.identity.web;

import id.aisnext.identity.application.RoleDirectoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Authenticated server-rendered UI for browsing legacy roles without modifying them. */
@Controller
public class RolePageController {
    private final RoleDirectoryService service;

    /**
     * Creates the role page controller.
     *
     * @param service role directory application service
     */
    public RolePageController(RoleDirectoryService service) { this.service = service; }

    /**
     * Renders the filtered and paginated role list.
     *
     * @param page zero-based page index
     * @param size requested page size
     * @param q case-insensitive role ID/name filter
     * @param model model receiving the page result and preserved query
     * @return {@code roles/index} Thymeleaf template name
     */
    @GetMapping("/roles") public String roles(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "25") int size,
                                               @RequestParam(defaultValue = "") String q,
                                               Model model) {
        model.addAttribute("result", service.find(page, size, q));
        model.addAttribute("query", q);
        return "roles/index";
    }

    /**
     * Renders one role detail or returns HTTP 404 when the role is absent.
     *
     * @param roleId exact legacy role identifier
     * @param model model receiving the role projection
     * @return {@code roles/detail} template name or a not-found response
     */
    @GetMapping("/roles/{roleId}") public Object role(@PathVariable String roleId, Model model) {
        return service.findOne(roleId).<Object>map(role -> {
            model.addAttribute("role", role);
            return "roles/detail";
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
