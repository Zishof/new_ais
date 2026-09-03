package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.WriteOwnership;

/**
 * Tenant-specific strangler route and its associated write-ownership guard.
 *
 * @param id control-plane primary key
 * @param tenantId owning tenant
 * @param moduleKey stable Spring Modulith/business module key
 * @param routePattern reverse-proxy or application route pattern
 * @param routeOwner application currently serving the route
 * @param writeOwnership aggregate write policy enforced for the route
 * @param version optimistic-lock version
 */
public record TenantModuleRoute(long id, TenantId tenantId, String moduleKey, String routePattern,
                                String routeOwner, WriteOwnership writeOwnership, long version) {}
