package id.aisnext.security.api;

import id.aisnext.tenant.api.TenantId;
import java.security.Principal;

/**
 * Authenticated session identity established from a consumed handoff token.
 *
 * @param tenantId tenant cryptographically bound into the token
 * @param userId legacy user identifier
 * @param activeRoleId role selected for this session
 */
public record HandoffPrincipal(TenantId tenantId, String userId, String activeRoleId) implements Principal {
    /**
     * Returns the stable principal name expected by Spring Security and audit code.
     *
     * @return the legacy user identifier
     */
    @Override public String getName() { return userId; }
}
