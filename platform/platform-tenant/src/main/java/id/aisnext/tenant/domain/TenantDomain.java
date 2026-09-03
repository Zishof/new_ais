package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;

public record TenantDomain(long id, TenantId tenantId, String domain, String normalizedDomain,
                           String type, String status, boolean primaryDomain) {}
