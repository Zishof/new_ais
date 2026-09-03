package id.aisnext.tenant.api;

/**
 * Mutually exclusive aggregate write-ownership states used during strangler migration.
 *
 * <p>At most one application is the primary writer in every state.</p>
 */
public enum WriteOwnership {
    /** Legacy is the sole writer; no AIS Next read parity has been approved. */
    LEGACY_WRITE,
    /** Legacy remains the writer while AIS Next provides an approved read projection. */
    NEXT_READ_ONLY,
    /** Legacy remains the writer while AIS Next evaluates non-authoritative shadow behavior. */
    NEXT_SHADOW,
    /** AIS Next is the sole primary writer for the aggregate. */
    NEXT_WRITE,
    /** AIS Next writes while legacy is retained only for verified read compatibility. */
    LEGACY_READ_ONLY,
    /** Legacy route and compatibility path have been retired after acceptance. */
    RETIRED
}
