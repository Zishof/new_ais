package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;

/**
 * Tenant-scoped feature gate stored independently of legacy application tables.
 *
 * @param id control-plane primary key
 * @param tenantId owning tenant
 * @param flagKey stable feature identifier
 * @param enabled effective flag state
 * @param version optimistic-lock version
 */
public record TenantFeatureFlag(long id, TenantId tenantId, String flagKey, boolean enabled, long version) {}
