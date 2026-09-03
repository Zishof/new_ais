package id.aisnext.config;

import id.aisnext.tenant.api.RouteOwner;
import id.aisnext.tenant.api.TenantRouteDecision;
import id.aisnext.tenant.api.WriteOwnership;
import java.util.List;

/** Single source of truth for the identity prefixes governed during the read-only phase. */
public final class PhaseTwoRoutes {
    private static final List<String> IDENTITY_READ_PREFIXES = List.of(
            "/roles",
            "/profile",
            "/search",
            "/api/v1/roles",
            "/api/v1/profile",
            "/api/v1/search");

    /** Prevents instantiation of this static route catalog. */
    private PhaseTwoRoutes() {
    }

    /**
     * Returns all request prefixes protected by an explicit tenant route decision.
     *
     * @return immutable list of Phase 2 identity route prefixes
     */
    public static List<String> identityReadPrefixes() {
        return IDENTITY_READ_PREFIXES;
    }

    /**
     * Builds the non-persistent fallback decisions used when the control plane is disabled.
     *
     * @return immutable AIS Next read-only decisions for every governed prefix
     */
    public static List<TenantRouteDecision> localNextReadOnlyDecisions() {
        return IDENTITY_READ_PREFIXES.stream()
                .map(prefix -> new TenantRouteDecision(
                        "identity", prefix, RouteOwner.NEXT, WriteOwnership.NEXT_READ_ONLY, 0L))
                .toList();
    }
}
