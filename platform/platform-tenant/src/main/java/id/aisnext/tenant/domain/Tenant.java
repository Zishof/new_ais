package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantMode;

public record Tenant(TenantId id, String code, String name, String slug, String status, TenantMode mode,
                     String defaultLocale, String timezone, long version) {}
