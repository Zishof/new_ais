package id.aisnext.identity.application;

import static org.assertj.core.api.Assertions.assertThat;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.legacycontract.api.LegacyIdentityQuery;
import id.aisnext.legacycontract.api.LegacyMenuPrivilege;
import id.aisnext.legacycontract.api.LegacyRoleDetail;
import id.aisnext.legacycontract.api.LegacyRoleQuery;
import id.aisnext.legacycontract.api.LegacyRoleSummary;
import id.aisnext.legacycontract.api.LegacyUserAccount;
import id.aisnext.security.api.HandoffPrincipal;
import id.aisnext.tenant.api.TenantId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Verifies that handoff claims are re-authorized against current legacy identity state. */
class HandoffAuthorizationServiceTest {
    private static final TenantId TENANT_ID = new TenantId("local");
    private static final LegacyUserAccount ADMIN =
            new LegacyUserAccount("admin", "Administrator", true, "am", List.of("am", "auditor"));
    private static final LegacyRoleDetail ACTIVE_ROLE = new LegacyRoleDetail(
            "am",
            "Administrator",
            true,
            List.of(new LegacyMenuPrivilege(
                    2L, "Grup Pengguna", "/roles", 1L, 2L,
                    true, true, true, true, false, false)));

    /** Creates the test fixture. */
    HandoffAuthorizationServiceTest() {}

    /** Confirms that authorities come from current effective legacy privileges. */
    @Test
    void derivesAuthoritiesFromCurrentLegacyRole() {
        LegacyIdentityQuery identities = userId -> "admin".equals(userId)
                ? Optional.of(ADMIN) : Optional.empty();
        HandoffAuthorizationService service =
                new HandoffAuthorizationService(identities, new StubLegacyRoleQuery(ACTIVE_ROLE));

        Optional<HandoffAuthorizationService.AuthorizedHandoff> result =
                service.authorize(new HandoffPrincipal(TENANT_ID, "admin", "am"));

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().authorities()).containsExactly(
                "ROLE_AM",
                "LEGACY_MENU_2_READ",
                "LEGACY_MENU_2_CREATE",
                "LEGACY_MENU_2_UPDATE",
                "LEGACY_MENU_2_DELETE");
    }

    /** Confirms that unknown accounts cannot create a session. */
    @Test
    void rejectsUnknownAccount() {
        HandoffAuthorizationService service = new HandoffAuthorizationService(
                userId -> Optional.empty(), new StubLegacyRoleQuery(ACTIVE_ROLE));

        assertThat(service.authorize(new HandoffPrincipal(TENANT_ID, "missing", "am"))).isEmpty();
    }

    /** Confirms that a signed but unassigned role cannot create a session. */
    @Test
    void rejectsRoleNotAssignedToAccount() {
        HandoffAuthorizationService service = new HandoffAuthorizationService(
                userId -> Optional.of(ADMIN), new StubLegacyRoleQuery(ACTIVE_ROLE));

        assertThat(service.authorize(new HandoffPrincipal(TENANT_ID, "admin", "Dosen"))).isEmpty();
    }

    /** Confirms that a disabled role cannot create a session. */
    @Test
    void rejectsInactiveRole() {
        LegacyRoleDetail inactive = new LegacyRoleDetail("am", "Administrator", false, List.of());
        HandoffAuthorizationService service = new HandoffAuthorizationService(
                userId -> Optional.of(ADMIN), new StubLegacyRoleQuery(inactive));

        assertThat(service.authorize(new HandoffPrincipal(TENANT_ID, "admin", "am"))).isEmpty();
    }

    /** Minimal deterministic role port used to isolate authorization behavior. */
    private static final class StubLegacyRoleQuery implements LegacyRoleQuery {
        private final LegacyRoleDetail role;

        /**
         * Creates a role query exposing one role.
         *
         * @param role role returned when its identifier matches
         */
        private StubLegacyRoleQuery(LegacyRoleDetail role) {
            this.role = role;
        }

        /**
         * Returns an empty page because list behavior is outside this test fixture.
         *
         * @param query validated page query
         * @return empty role page
         */
        @Override
        public PageResult<LegacyRoleSummary> findRoles(PageQuery query) {
            return new PageResult<>(List.of(), query.page(), query.size(), 0L);
        }

        /**
         * Returns the fixture role only for its exact identifier.
         *
         * @param roleId requested role identifier
         * @return matching fixture role, or empty
         */
        @Override
        public Optional<LegacyRoleDetail> findRole(String roleId) {
            return role.id().equals(roleId) ? Optional.of(role) : Optional.empty();
        }
    }
}
