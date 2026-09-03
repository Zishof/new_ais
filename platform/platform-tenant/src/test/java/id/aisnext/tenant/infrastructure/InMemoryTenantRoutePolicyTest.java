package id.aisnext.tenant.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import id.aisnext.tenant.api.RouteOwner;
import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantRouteDecision;
import id.aisnext.tenant.api.WriteOwnership;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Verifies deterministic longest-prefix matching for the in-memory route policy. */
class InMemoryTenantRoutePolicyTest {
    /** Creates the test fixture. */
    InMemoryTenantRoutePolicyTest() {
    }

    /** Confirms that the most-specific boundary match wins regardless of input order. */
    @Test
    void selectsLongestMatchingPrefix() {
        InMemoryTenantRoutePolicy policy = new InMemoryTenantRoutePolicy(List.of(
                decision("/api/v1", RouteOwner.LEGACY),
                decision("/api/v1/roles", RouteOwner.NEXT)));

        assertThat(policy.findRoute(new TenantId("local"), "/api/v1/roles/am"))
                .contains(decision("/api/v1/roles", RouteOwner.NEXT));
    }

    /** Confirms that a similar text prefix without a path boundary does not match. */
    @Test
    void requiresPathBoundary() {
        InMemoryTenantRoutePolicy policy =
                new InMemoryTenantRoutePolicy(List.of(decision("/roles", RouteOwner.NEXT)));

        assertThat(policy.findRoute(new TenantId("local"), "/roles-extra")).isEmpty();
    }

    /**
     * Creates a read-only fixture decision.
     *
     * @param prefix route prefix
     * @param owner route owner
     * @return immutable fixture decision
     */
    private static TenantRouteDecision decision(String prefix, RouteOwner owner) {
        return new TenantRouteDecision("identity", prefix, owner, WriteOwnership.NEXT_READ_ONLY, 0L);
    }
}
