package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;

/**
 * Non-secret pointer from tenant metadata to an external secret provider.
 *
 * @param id control-plane primary key
 * @param tenantId owning tenant
 * @param referenceKey stable application-facing reference
 * @param provider secret-manager/environment provider type
 * @param providerPath provider-specific path that contains no secret value
 */
public record TenantSecretReference(long id, TenantId tenantId, String referenceKey, String provider, String providerPath) {}
