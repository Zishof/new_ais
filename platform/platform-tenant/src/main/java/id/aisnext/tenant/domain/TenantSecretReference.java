package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;

public record TenantSecretReference(long id, TenantId tenantId, String referenceKey, String provider, String providerPath) {}
