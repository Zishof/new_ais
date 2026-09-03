package id.aisnext.identity.application;

import id.aisnext.legacycontract.api.LegacyIdentityQuery;
import id.aisnext.legacycontract.api.LegacyMenuPrivilege;
import id.aisnext.legacycontract.api.LegacyRoleDetail;
import id.aisnext.legacycontract.api.LegacyRoleQuery;
import id.aisnext.legacycontract.api.LegacyUserAccount;
import id.aisnext.security.api.HandoffPrincipal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Validates handoff identity claims against the active legacy account and role assignments.
 *
 * <p>A valid token proves only that a trusted issuer created the claims. This service additionally
 * proves that the referenced user is still active, that the selected role is assigned to that
 * user, and that the role itself is active. Effective legacy menu capabilities are translated to
 * Spring Security authority names so authorization remains enforced on the server.</p>
 */
@Service
public class HandoffAuthorizationService {
    /** Legacy menu identifier for the "Grup Pengguna" feature. */
    public static final long ROLE_DIRECTORY_MENU_ID = 2L;

    /** Read authority required by the role-directory UI, API, and search provider. */
    public static final String ROLE_DIRECTORY_READ_AUTHORITY = "LEGACY_MENU_2_READ";

    private final LegacyIdentityQuery identities;
    private final LegacyRoleQuery roles;

    /**
     * Creates the handoff authorization service from immutable legacy read ports.
     *
     * @param identities active legacy account projection port
     * @param roles legacy role and effective menu privilege projection port
     */
    public HandoffAuthorizationService(LegacyIdentityQuery identities, LegacyRoleQuery roles) {
        this.identities = identities;
        this.roles = roles;
    }

    /**
     * Validates a handoff principal and derives its effective server-side authorities.
     *
     * @param principal cryptographically verified token principal bound to the trusted tenant
     * @return authorized account and immutable authorities, or empty when identity/role validation fails
     */
    public Optional<AuthorizedHandoff> authorize(HandoffPrincipal principal) {
        Optional<LegacyUserAccount> account = identities.findActiveUser(principal.userId());
        if (account.isEmpty() || !account.orElseThrow().roleIds().contains(principal.activeRoleId())) {
            return Optional.empty();
        }
        Optional<LegacyRoleDetail> role = roles.findRole(principal.activeRoleId())
                .filter(LegacyRoleDetail::active);
        return role.map(detail -> new AuthorizedHandoff(account.orElseThrow(), authorities(detail)));
    }

    /**
     * Converts a role identifier and all enabled menu operations to stable authority strings.
     *
     * @param role active legacy role and its effective menu capabilities
     * @return deterministic immutable authority list
     */
    private static List<String> authorities(LegacyRoleDetail role) {
        Set<String> authorities = new LinkedHashSet<>();
        authorities.add("ROLE_" + role.id().replaceAll("[^A-Za-z0-9_]", "_").toUpperCase(Locale.ROOT));
        for (LegacyMenuPrivilege menu : role.menus()) {
            addCapability(authorities, menu.menuId(), "READ", menu.readable());
            addCapability(authorities, menu.menuId(), "CREATE", menu.creatable());
            addCapability(authorities, menu.menuId(), "UPDATE", menu.updatable());
            addCapability(authorities, menu.menuId(), "DELETE", menu.deletable());
            addCapability(authorities, menu.menuId(), "APPROVE", menu.approvable());
            addCapability(authorities, menu.menuId(), "REJECT", menu.rejectable());
        }
        return List.copyOf(authorities);
    }

    /**
     * Adds one menu-operation authority when the legacy capability is enabled.
     *
     * @param authorities mutable insertion-ordered authority set
     * @param menuId immutable legacy menu identifier
     * @param operation normalized capability suffix
     * @param enabled whether the legacy role grants the capability
     */
    private static void addCapability(Set<String> authorities, long menuId, String operation, boolean enabled) {
        if (enabled) {
            authorities.add("LEGACY_MENU_" + menuId + "_" + operation);
        }
    }

    /**
     * Result of validating the current legacy identity and selected role.
     *
     * @param account active legacy user projection without credential fields
     * @param authorities immutable effective authorities derived from the selected legacy role
     */
    public record AuthorizedHandoff(LegacyUserAccount account, List<String> authorities) {
        /** Creates an authorization result with a defensive authority-list copy. */
        public AuthorizedHandoff {
            authorities = List.copyOf(authorities);
        }
    }
}
