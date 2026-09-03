package id.aisnext.tenant.api;

import java.util.Optional;

public final class TenantContext {
    private static final ThreadLocal<ResolvedTenant> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static Scope open(ResolvedTenant tenant) {
        if (CURRENT.get() != null) throw new IllegalStateException("tenant context is already bound");
        CURRENT.set(tenant);
        return new Scope();
    }

    public static Optional<ResolvedTenant> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static ResolvedTenant require() {
        ResolvedTenant tenant = CURRENT.get();
        if (tenant == null) throw new IllegalStateException("tenant context is not bound");
        return tenant;
    }

    public static final class Scope implements AutoCloseable {
        private boolean closed;
        @Override public void close() {
            if (!closed) {
                CURRENT.remove();
                closed = true;
            }
        }
    }
}
