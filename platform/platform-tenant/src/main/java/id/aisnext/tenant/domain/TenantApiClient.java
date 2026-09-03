package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;

/**
 * Control-plane metadata for a tenant-scoped machine API client.
 *
 * @param id control-plane primary key
 * @param tenantId owning tenant
 * @param clientId public client identifier
 * @param secretHash one-way hash of the client secret; never plaintext
 * @param status client lifecycle status
 */
public record TenantApiClient(long id, TenantId tenantId, String clientId, String secretHash, String status) {}
