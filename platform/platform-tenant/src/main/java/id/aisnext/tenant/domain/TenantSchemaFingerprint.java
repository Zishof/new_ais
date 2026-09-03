package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.DatabaseRole;
import id.aisnext.tenant.api.TenantId;
import java.time.Instant;

public record TenantSchemaFingerprint(long id, TenantId tenantId, DatabaseRole role, String algorithm,
                                      String fingerprint, Instant capturedAt) {}
