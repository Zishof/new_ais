package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantMode;

/**
 * Control-plane representation of one institution/tenant.
 *
 * @param id stable external tenant key
 * @param code operator-facing institution code
 * @param name human-readable institution name
 * @param slug URL-safe tenant slug
 * @param status provisioning/operational status
 * @param mode current legacy/Next coexistence mode
 * @param defaultLocale locale tag used when no user preference exists
 * @param timezone IANA time-zone identifier
 * @param version optimistic-lock version
 */
public record Tenant(TenantId id, String code, String name, String slug, String status, TenantMode mode,
                     String defaultLocale, String timezone, long version) {}
