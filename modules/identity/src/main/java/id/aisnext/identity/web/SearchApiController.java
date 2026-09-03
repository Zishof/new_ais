package id.aisnext.identity.web;

import id.aisnext.identity.application.GlobalSearchService;
import id.aisnext.identity.application.SearchResult;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Versioned REST API for bounded search across currently migrated providers.
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchApiController {
    private final GlobalSearchService search;

    /**
     * Creates the global search API controller.
     *
     * @param search bounded authorized search service
     */
    public SearchApiController(GlobalSearchService search) {
        this.search = search;
    }

    /**
     * Returns navigation-safe search results for an authorized caller.
     *
     * @param query user-entered search text
     * @return at most ten results from currently migrated providers
     */
    @GetMapping
    public List<SearchResult> search(@RequestParam(name = "q", defaultValue = "") String query) {
        return search.search(query);
    }
}
