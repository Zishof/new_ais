package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;

public record TenantApiClient(long id, TenantId tenantId, String clientId, String secretHash, String status) {}
