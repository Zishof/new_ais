package id.aisnext.tenant.api;

public enum WriteOwnership {
    LEGACY_WRITE,
    NEXT_READ_ONLY,
    NEXT_SHADOW,
    NEXT_WRITE,
    LEGACY_READ_ONLY,
    RETIRED
}
