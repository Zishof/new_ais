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

/** Versioned REST API for authenticated, read-only access to the legacy role directory. */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleApiController {
    private final RoleDirectoryService service;

    /**
     * Creates the role REST controller.
     *
     * @param service role directory application service
     */
    public RoleApiController(RoleDirectoryService service) { this.service = service; }

    /**
     * Returns a filtered, server-paged list of role summaries.
     *
     * @param page zero-based page index
     * @param size requested page size
     * @param q case-insensitive role ID/name filter
     * @return page payload with total count
     */
    @GetMapping public PageResult<LegacyRoleSummary> roles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "") String q) {
        return service.find(page, size, q);
    }

    /**
     * Returns one role and its effective legacy menu privileges.
     *
     * @param roleId exact legacy role identifier from the path
     * @return HTTP 200 with detail or HTTP 404 when absent
     */
    @GetMapping("/{roleId}") public ResponseEntity<LegacyRoleDetail> role(@PathVariable String roleId) {
        return ResponseEntity.of(service.findOne(roleId));
    }
}
