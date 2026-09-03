package id.aisnext.websupport.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import id.aisnext.tenant.api.ResolvedTenant;
import id.aisnext.tenant.api.RouteOwner;
import id.aisnext.tenant.api.TenantContext;
import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantMode;
import id.aisnext.tenant.api.TenantRouteDecision;
import id.aisnext.tenant.api.TenantRoutePolicy;
import id.aisnext.tenant.api.WriteOwnership;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Verifies the fail-closed servlet enforcement of tenant route ownership. */
class TenantRouteGateFilterTest {
    private static final TenantId TENANT_ID = new TenantId("local");
    private static final ResolvedTenant TENANT = new ResolvedTenant(
            TENANT_ID, "AIS Local", TenantMode.HYBRID,
            Locale.forLanguageTag("id-ID"), ZoneId.of("Asia/Jakarta"));

    /** Creates the test fixture. */
    TenantRouteGateFilterTest() {
    }

    /**
     * Confirms that AIS Next ownership allows the request to reach the application.
     *
     * @throws Exception when servlet filtering fails
     */
    @Test
    void allowsNextOwnedRoute() throws Exception {
        MockFilterChain chain = execute(policy(RouteOwner.NEXT), "/roles/am");

        assertThat(chain.getRequest()).isNotNull();
    }

    /**
     * Confirms that legacy ownership hides the stale AIS Next endpoint.
     *
     * @throws Exception when servlet filtering fails
     */
    @Test
    void blocksLegacyOwnedRoute() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = execute(policy(RouteOwner.LEGACY), "/roles", response);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(chain.getRequest()).isNull();
    }

    /**
     * Confirms that missing metadata fails closed for a governed route.
     *
     * @throws Exception when servlet filtering fails
     */
    @Test
    void blocksGovernedRouteWithoutDecision() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = execute((tenantId, path) -> Optional.empty(), "/roles", response);

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(chain.getRequest()).isNull();
    }

    /**
     * Confirms that unrelated application paths do not invoke the route policy.
     *
     * @throws Exception when servlet filtering fails
     */
    @Test
    void ignoresUngovernedRoute() throws Exception {
        TenantRoutePolicy unused = (tenantId, path) -> {
            throw new AssertionError("policy must not be queried");
        };
        TenantRouteGateFilter filter = new TenantRouteGateFilter(unused, List.of("/roles"));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest("GET", "/dashboard"),
                new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    /**
     * Creates a route policy containing one fixture decision.
     *
     * @param owner owner returned by the fixture policy
     * @return route policy matching the roles prefix
     */
    private static TenantRoutePolicy policy(RouteOwner owner) {
        TenantRouteDecision decision = new TenantRouteDecision(
                "identity", "/roles", owner, WriteOwnership.NEXT_READ_ONLY, 0L);
        return (tenantId, path) -> decision.matches(path) ? Optional.of(decision) : Optional.empty();
    }

    /**
     * Executes a governed request with a fresh response.
     *
     * @param policy route policy under test
     * @param path governed request path
     * @return filter chain containing the delegated request when allowed
     * @throws Exception when servlet filtering fails
     */
    private static MockFilterChain execute(TenantRoutePolicy policy, String path) throws Exception {
        return execute(policy, path, new MockHttpServletResponse());
    }

    /**
     * Executes a governed request within a trusted tenant context.
     *
     * @param policy route policy under test
     * @param path governed request path
     * @param response response receiving gate status
     * @return filter chain containing the delegated request when allowed
     * @throws Exception when servlet filtering fails
     */
    private static MockFilterChain execute(TenantRoutePolicy policy, String path,
                                           MockHttpServletResponse response) throws Exception {
        TenantRouteGateFilter filter = new TenantRouteGateFilter(policy, List.of("/roles"));
        MockFilterChain chain = new MockFilterChain();
        try (TenantContext.Scope ignored = TenantContext.open(TENANT)) {
            filter.doFilter(new MockHttpServletRequest("GET", path), response, chain);
        }
        return chain;
    }
}
