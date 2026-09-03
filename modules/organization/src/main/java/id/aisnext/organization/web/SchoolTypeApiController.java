package id.aisnext.organization.web;

import id.aisnext.kernel.api.PageResult;
import id.aisnext.organization.api.SchoolLevel;
import id.aisnext.organization.api.SchoolType;
import id.aisnext.organization.api.SchoolTypeSort;
import id.aisnext.organization.application.SchoolTypeService;
import id.aisnext.organization.domain.SchoolTypeNotFoundException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Versioned REST boundary for the clone-gated school-type aggregate. */
@RestController
@RequestMapping("/api/v1/school-types")
public class SchoolTypeApiController {
    private final SchoolTypeService service;

    /**
     * Creates the REST controller.
     *
     * @param service school-type application service
     */
    public SchoolTypeApiController(SchoolTypeService service) {
        this.service = service;
    }

    /**
     * Returns a filtered, sorted, server-paged catalogue.
     *
     * @param page zero-based page index
     * @param size requested page size
     * @param q case-insensitive name fragment
     * @param sort whitelisted sort name
     * @param activeOnly whether inactive rows are excluded
     * @return result page
     */
    @GetMapping
    public PageResult<SchoolType> list(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "25") int size,
                                       @RequestParam(defaultValue = "") String q,
                                       @RequestParam(defaultValue = "NAME_ASC") String sort,
                                       @RequestParam(defaultValue = "false") boolean activeOnly) {
        return service.findAll(page, size, q, SchoolTypeSort.parse(sort), activeOnly);
    }

    /**
     * Returns level options used by create and edit clients.
     *
     * @return active or currently referenced levels
     */
    @GetMapping("/levels")
    public List<SchoolLevel> levels() {
        return service.selectableLevels();
    }

    /**
     * Returns one school type with a standards-compliant quoted ETag.
     *
     * @param id legacy primary key
     * @return detail response or 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<SchoolType> detail(@PathVariable long id) {
        return service.findOne(id).map(value -> ResponseEntity.ok().eTag(value.versionToken()).body(value))
                .orElseThrow(() -> new SchoolTypeNotFoundException(id));
    }

    /**
     * Creates one school type as the authenticated actor.
     *
     * @param request untrusted JSON fields
     * @param principal authenticated handoff principal
     * @return HTTP 201 response with location, ETag, and created projection
     */
    @PostMapping
    public ResponseEntity<SchoolType> create(@RequestBody SchoolTypeRequest request, Principal principal) {
        SchoolType created = service.create(request.toCommand(), principal.getName());
        return ResponseEntity.created(URI.create("/api/v1/school-types/" + created.id()))
                .eTag(created.versionToken()).body(created);
    }

    /**
     * Updates one school type only if the submitted ETag is still current.
     *
     * @param id legacy primary key
     * @param version quoted or unquoted {@code If-Match} snapshot token
     * @param request untrusted JSON fields
     * @param principal authenticated handoff principal
     * @return updated projection and replacement ETag
     */
    @PutMapping("/{id}")
    public ResponseEntity<SchoolType> update(@PathVariable long id,
                                              @RequestHeader("If-Match") String version,
                                              @RequestBody SchoolTypeRequest request,
                                              Principal principal) {
        SchoolType updated = service.update(id, unquote(version), request.toCommand(), principal.getName());
        return ResponseEntity.ok().eTag(updated.versionToken()).body(updated);
    }

    /**
     * Deletes one unreferenced school type only if the submitted ETag is current.
     *
     * @param id legacy primary key
     * @param version quoted or unquoted {@code If-Match} snapshot token
     * @param principal authenticated handoff principal
     * @return empty HTTP 204 response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id,
                                        @RequestHeader("If-Match") String version,
                                        Principal principal) {
        service.delete(id, unquote(version), principal.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Removes one optional HTTP ETag quote pair without altering the token content.
     *
     * @param value raw {@code If-Match} header value
     * @return unquoted token
     */
    private static String unquote(String value) {
        if (value != null && value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
