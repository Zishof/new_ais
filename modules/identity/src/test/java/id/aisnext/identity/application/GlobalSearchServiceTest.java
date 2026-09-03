package id.aisnext.identity.application;

import static org.assertj.core.api.Assertions.assertThat;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.legacycontract.api.LegacyRoleDetail;
import id.aisnext.legacycontract.api.LegacyRoleQuery;
import id.aisnext.legacycontract.api.LegacyRoleSummary;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Verifies global-search query bounds and navigation-safe result links. */
class GlobalSearchServiceTest {
    /** Creates the test fixture. */
    GlobalSearchServiceTest() {}

    /** Confirms that one-character searches do not query the legacy projection. */
    @Test
    void ignoresQueriesShorterThanTwoCharacters() {
        GlobalSearchService service = new GlobalSearchService(
                new RoleDirectoryService(new SearchRoleQuery()));

        assertThat(service.search("a")).isEmpty();
        assertThat(service.search(" ")).isEmpty();
        assertThat(service.search(null)).isEmpty();
    }

    /** Confirms that role identifiers are safely encoded in result navigation URLs. */
    @Test
    void encodesRoleIdentifierInResultUrl() {
        GlobalSearchService service = new GlobalSearchService(
                new RoleDirectoryService(new SearchRoleQuery()));

        List<SearchResult> results = service.search("role");

        assertThat(results).singleElement().satisfies(result -> {
            assertThat(result.id()).isEqualTo("role/with space");
            assertThat(result.url()).isEqualTo("/roles?q=role%2Fwith+space");
        });
    }

    /** Fixed role projection used to isolate search-result composition. */
    private static final class SearchRoleQuery implements LegacyRoleQuery {
        /** Creates the fixed role projection. */
        private SearchRoleQuery() {}

        /**
         * Returns one matching role summary.
         *
         * @param query validated query supplied by the service
         * @return page containing one fixture role
         */
        @Override
        public PageResult<LegacyRoleSummary> findRoles(PageQuery query) {
            return new PageResult<>(
                    List.of(new LegacyRoleSummary("role/with space", "Role with space", true)),
                    query.page(), query.size(), 1L);
        }

        /**
         * Returns no role detail because search only needs summaries.
         *
         * @param roleId requested role identifier
         * @return always empty
         */
        @Override
        public Optional<LegacyRoleDetail> findRole(String roleId) {
            return Optional.empty();
        }
    }
}
