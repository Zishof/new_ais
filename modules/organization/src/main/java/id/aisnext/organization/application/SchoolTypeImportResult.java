package id.aisnext.organization.application;

/**
 * Atomic workbook-import outcome.
 *
 * @param created number of newly inserted school types
 * @param updated number of existing school types updated with current tokens
 */
public record SchoolTypeImportResult(int created, int updated) {
    /**
     * Validates non-negative import counters.
     *
     * @throws IllegalArgumentException when either counter is negative
     */
    public SchoolTypeImportResult {
        if (created < 0 || updated < 0) throw new IllegalArgumentException("import counts must not be negative");
    }

    /**
     * Returns the total committed row count.
     *
     * @return sum of created and updated rows
     */
    public int total() {
        return Math.addExact(created, updated);
    }
}
