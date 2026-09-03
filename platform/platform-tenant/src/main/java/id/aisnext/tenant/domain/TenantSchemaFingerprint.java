package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.DatabaseRole;
import id.aisnext.tenant.api.TenantId;
import java.time.Instant;

/**
 * Immutable evidence of a tenant database schema contract at a point in time.
 *
 * @param id control-plane primary key
 * @param tenantId owning tenant
 * @param role fingerprinted CORE or FILE database role
 * @param algorithm canonicalization and digest algorithm identifier
 * @param fingerprint resulting digest
 * @param capturedAt UTC capture time
 */
public record TenantSchemaFingerprint(long id, TenantId tenantId, DatabaseRole role, String algorithm,
                                      String fingerprint, Instant capturedAt) {}
