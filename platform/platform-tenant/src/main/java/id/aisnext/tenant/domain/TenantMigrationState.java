package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.WriteOwnership;

/**
 * Aggregate-level migration state that enforces one primary writer during coexistence.
 *
 * @param id control-plane primary key
 * @param tenantId owning tenant
 * @param aggregateKey stable aggregate type/key pattern
 * @param ownership current write-ownership state
 * @param version optimistic-lock version
 */
public record TenantMigrationState(long id, TenantId tenantId, String aggregateKey,
                                   WriteOwnership ownership, long version) {}
