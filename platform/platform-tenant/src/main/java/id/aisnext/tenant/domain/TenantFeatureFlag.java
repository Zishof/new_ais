package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;

public record TenantFeatureFlag(long id, TenantId tenantId, String flagKey, boolean enabled, long version) {}
