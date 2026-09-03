package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.DatabaseRole;
import id.aisnext.tenant.api.TenantId;

/**
 * Persisted non-secret descriptor of one tenant database.
 *
 * @param id control-plane primary key
 * @param tenantId owning tenant
 * @param role logical CORE or FILE role
 * @param jdbcUrl PostgreSQL connection URL
 * @param credentialReference secret-provider reference, never credential material
 * @param readOnly whether connections must reject writes
 * @param maximumPoolSize per-descriptor connection bound
 */
public record TenantDatabase(long id, TenantId tenantId, DatabaseRole role, String jdbcUrl,
                             String credentialReference, boolean readOnly, int maximumPoolSize) {}
