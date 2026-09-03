package id.aisnext.kernel.api;

import java.util.List;

/**
 * Immutable page payload with sufficient metadata for API and UI navigation.
 *
 * @param items defensive copy of items in deterministic query order
 * @param page zero-based page index
 * @param size requested page size
 * @param total total matching items across all pages
 * @param <T> projection type contained by the page
 */
public record PageResult<T>(List<T> items, int page, int size, long total) {
    /**
     * Copies item storage and validates non-negative page metadata.
     *
     * @throws NullPointerException when {@code items} is {@code null}
     * @throws IllegalArgumentException when page, size, or total is invalid
     */
    public PageResult {
        items = List.copyOf(items);
        if (page < 0 || size < 1 || total < 0) throw new IllegalArgumentException("invalid page metadata");
    }

    /**
     * Calculates the number of pages using ceiling division.
     *
     * @return zero for an empty result, otherwise at least one page
     */
    public long totalPages() {
        return total == 0 ? 0 : (total + size - 1) / size;
    }
}
