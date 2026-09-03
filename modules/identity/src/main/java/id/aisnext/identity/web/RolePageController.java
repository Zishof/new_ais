package id.aisnext.identity.web;

import id.aisnext.identity.application.RoleDirectoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RolePageController {
    private final RoleDirectoryService service;
    public RolePageController(RoleDirectoryService service) { this.service = service; }

    @GetMapping("/roles") public String roles(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "25") int size,
                                               @RequestParam(defaultValue = "") String q,
                                               Model model) {
        model.addAttribute("result", service.find(page, size, q));
        model.addAttribute("query", q);
        return "roles/index";
    }

    @GetMapping("/roles/{roleId}") public Object role(@PathVariable String roleId, Model model) {
        return service.findOne(roleId).<Object>map(role -> {
            model.addAttribute("role", role);
            return "roles/detail";
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
