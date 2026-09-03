package id.aisnext.supporting.web;

import id.aisnext.kernel.api.PageResult;
import id.aisnext.supporting.api.LibraryItemTypeEntry;
import id.aisnext.supporting.application.LibraryItemTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Versioned JSON boundary for the data-minimized, read-only library item-type directory. */
@RestController
@RequestMapping("/api/v1/supporting/library/item-types")
public class LibraryItemTypeApiController {
    private final LibraryItemTypeService service;

    /**
     * Creates the library item-type API controller.
     *
     * @param service validated library item-type application boundary
     */
    public LibraryItemTypeApiController(LibraryItemTypeService service) {
        this.service = service;
    }

    /**
     * Returns one deterministic page of global library item types.
     *
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param query literal item-type name-or-description fragment
     * @return immutable page containing only the three approved fields
     */
    @GetMapping
    public PageResult<LibraryItemTypeEntry> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(name = "q", defaultValue = "") String query) {
        return service.findItemTypes(page, size, query);
    }
}
