package id.aisnext.tenant.domain;

import id.aisnext.tenant.api.TenantId;

/**
 * Trusted host mapping used as the first tenant-resolution boundary.
 *
 * @param id control-plane primary key
 * @param tenantId tenant selected by the host
 * @param domain operator-entered domain
 * @param normalizedDomain lowercase lookup value without trailing dot
 * @param type domain classification
 * @param status mapping lifecycle status
 * @param primaryDomain whether this is the tenant's canonical host
 */
public record TenantDomain(long id, TenantId tenantId, String domain, String normalizedDomain,
                           String type, String status, boolean primaryDomain) {}
