package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.WriteOwnership;

public record TenantModuleRoute(long id, TenantId tenantId, String moduleKey, String routePattern,
                                String routeOwner, WriteOwnership writeOwnership, long version) {}
