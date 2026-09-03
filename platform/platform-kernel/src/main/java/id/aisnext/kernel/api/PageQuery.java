package id.aisnext.kernel.api;

public record PageQuery(int page, int size, String filter) {
    public PageQuery {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 200) throw new IllegalArgumentException("size must be between 1 and 200");
        filter = filter == null ? "" : filter.trim();
    }

    public int offset() {
        return Math.multiplyExact(page, size);
    }
}
