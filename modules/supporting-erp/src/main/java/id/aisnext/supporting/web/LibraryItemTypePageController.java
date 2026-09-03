package id.aisnext.supporting.web;

import id.aisnext.supporting.application.LibraryItemTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Accessible server-rendered boundary for the minimized library item-type directory. */
@Controller
public class LibraryItemTypePageController {
    private final LibraryItemTypeService service;

    /**
     * Creates the library item-type page controller.
     *
     * @param service validated library item-type application boundary
     */
    public LibraryItemTypePageController(LibraryItemTypeService service) {
        this.service = service;
    }

    /**
     * Renders one item-type page while preserving its filter and paging state.
     *
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param query literal item-type name-or-description fragment
     * @param model template model receiving the page and preserved filter
     * @return library item-type directory template name
     */
    @GetMapping("/supporting/library/item-types")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "25") int size,
                       @RequestParam(name = "q", defaultValue = "") String query,
                       Model model) {
        model.addAttribute("result", service.findItemTypes(page, size, query));
        model.addAttribute("query", query == null ? "" : query.trim());
        return "supporting/library-item-types";
    }
}
