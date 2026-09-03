package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.DatabaseRole;
import id.aisnext.tenant.api.TenantId;

public record TenantDatabase(long id, TenantId tenantId, DatabaseRole role, String jdbcUrl,
                             String credentialReference, boolean readOnly, int maximumPoolSize) {}
