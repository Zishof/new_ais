package id.aisnext.tenant.api;

/** High-level coexistence mode for routing a tenant between legacy and AIS Next. */
public enum TenantMode {
    /** All business routes remain owned by AIS legacy. */
    LEGACY,
    /** Route ownership is split by module or capability during strangler migration. */
    HYBRID,
    /** All approved routes are owned by tenant-aware AIS Next implementations. */
    TENANT_ONLY
}
