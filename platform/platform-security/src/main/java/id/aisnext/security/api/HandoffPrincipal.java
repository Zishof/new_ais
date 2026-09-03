package id.aisnext.security.api;

import id.aisnext.tenant.api.TenantId;
import java.security.Principal;

public record HandoffPrincipal(TenantId tenantId, String userId, String activeRoleId) implements Principal {
    @Override public String getName() { return userId; }
}
