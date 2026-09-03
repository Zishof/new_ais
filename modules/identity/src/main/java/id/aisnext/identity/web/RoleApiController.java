package id.aisnext.identity.web;

import id.aisnext.identity.application.RoleDirectoryService;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.legacycontract.api.LegacyRoleDetail;
import id.aisnext.legacycontract.api.LegacyRoleSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleApiController {
    private final RoleDirectoryService service;
    public RoleApiController(RoleDirectoryService service) { this.service = service; }

    @GetMapping public PageResult<LegacyRoleSummary> roles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "") String q) {
        return service.find(page, size, q);
    }

    @GetMapping("/{roleId}") public ResponseEntity<LegacyRoleDetail> role(@PathVariable String roleId) {
        return ResponseEntity.of(service.findOne(roleId));
    }
}
