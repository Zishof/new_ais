package id.aisnext.tenant.api;

import java.util.Optional;

/**
 * Thread-bound tenant context established once per trusted HTTP request.
 *
 * <p>Callers must use try-with-resources around {@link #open(ResolvedTenant)}. Nested replacement is
 * rejected to prevent cross-tenant confusion, and {@link Scope#close()} always removes the thread
 * value so pooled servlet threads cannot leak tenant identity.</p>
 */
public final class TenantContext {
    private static final ThreadLocal<ResolvedTenant> CURRENT = new ThreadLocal<>();

    /** Prevents instantiation of this static context holder. */
    private TenantContext() {}

    /**
     * Binds one tenant to the current thread until the returned scope is closed.
     *
     * @param tenant trusted, fully resolved tenant
     * @return closeable scope that removes the binding exactly once
     * @throws IllegalStateException when a tenant is already bound to this thread
     */
    public static Scope open(ResolvedTenant tenant) {
        if (CURRENT.get() != null) throw new IllegalStateException("tenant context is already bound");
        CURRENT.set(tenant);
        return new Scope();
    }

    /**
     * Reads the current binding without requiring one.
     *
     * @return current tenant, or empty outside a tenant-bound operation
     */
    public static Optional<ResolvedTenant> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * Reads the tenant required by routing or authorization code.
     *
     * @return tenant bound to the current thread
     * @throws IllegalStateException when no tenant has been established
     */
    public static ResolvedTenant require() {
        ResolvedTenant tenant = CURRENT.get();
        if (tenant == null) throw new IllegalStateException("tenant context is not bound");
        return tenant;
    }

    /** Idempotent closeable lifetime token for one thread-bound tenant context. */
    public static final class Scope implements AutoCloseable {
        private boolean closed;

        /**
         * Creates an open scope token.
         *
         * <p>Application code normally obtains a token from {@link TenantContext#open(ResolvedTenant)}
         * instead of constructing one directly.</p>
         */
        public Scope() {
        }

        /** Removes the current thread's tenant binding; repeated calls have no effect. */
        @Override public void close() {
            if (!closed) {
                CURRENT.remove();
                closed = true;
            }
        }
    }
}
