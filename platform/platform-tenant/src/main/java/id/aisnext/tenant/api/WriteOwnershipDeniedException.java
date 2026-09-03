package id.aisnext.tenant.api;

/** Raised when a command reaches AIS Next without explicit {@code NEXT_WRITE} ownership. */
public final class WriteOwnershipDeniedException extends RuntimeException {
    /**
     * Creates a denial containing an operator-safe ownership explanation.
     *
     * @param message explanation that identifies the denied tenant/aggregate without credentials
     */
    public WriteOwnershipDeniedException(String message) {
        super(message);
    }
}
