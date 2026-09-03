package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.WriteOwnership;

public record TenantMigrationState(long id, TenantId tenantId, String aggregateKey,
                                   WriteOwnership ownership, long version) {}
