package id.aisnext.kernel.api;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long total) {
    public PageResult {
        items = List.copyOf(items);
        if (page < 0 || size < 1 || total < 0) throw new IllegalArgumentException("invalid page metadata");
    }

    public long totalPages() {
        return total == 0 ? 0 : (total + size - 1) / size;
    }
}
