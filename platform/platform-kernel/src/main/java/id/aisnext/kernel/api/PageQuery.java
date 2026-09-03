package id.aisnext.kernel.api;

/**
 * Validated offset-pagination request shared by API and server-rendered read models.
 *
 * @param page zero-based page index
 * @param size number of items, constrained to 1 through 200
 * @param filter trimmed free-text filter; {@code null} is normalized to empty
 */
public record PageQuery(int page, int size, String filter) {
    /**
     * Validates bounds and normalizes the text filter.
     *
     * @throws IllegalArgumentException when page is negative or size is outside 1 through 200
     */
    public PageQuery {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 200) throw new IllegalArgumentException("size must be between 1 and 200");
        filter = filter == null ? "" : filter.trim();
    }

    /**
     * Calculates the SQL row offset with overflow detection.
     *
     * @return {@code page * size}
     * @throws ArithmeticException if the multiplication overflows an integer
     */
    public int offset() {
        return Math.multiplyExact(page, size);
    }
}
