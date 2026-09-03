package id.aisnext.identity.web;

import id.aisnext.identity.application.GlobalSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Server-rendered entry point for authorized cross-module search results.
 */
@Controller
public class SearchPageController {
    private final GlobalSearchService search;

    /**
     * Creates the global search page controller.
     *
     * @param search bounded authorized search service
     */
    public SearchPageController(GlobalSearchService search) {
        this.search = search;
    }

    /**
     * Renders search results from all currently migrated and authorized providers.
     *
     * @param query user-entered search text
     * @param model model receiving the normalized query and bounded result list
     * @return {@code search/index} Thymeleaf template name
     */
    @GetMapping("/search")
    public String search(@RequestParam(name = "q", defaultValue = "") String query, Model model) {
        model.addAttribute("query", query == null ? "" : query.trim());
        model.addAttribute("results", search.search(query));
        return "search/index";
    }
}
