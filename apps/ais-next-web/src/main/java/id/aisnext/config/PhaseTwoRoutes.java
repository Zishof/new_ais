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
    private static final List<String> ORGANIZATION_WRITE_PREFIXES = List.of(
            "/school-types",
            "/api/v1/school-types");
    private static final List<String> ATTENDANCE_READ_PREFIXES = List.of(
            "/attendance/daily",
            "/api/v1/attendance/daily");

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
     * Returns organization prefixes reserved for the clone-only Phase 3 write slice.
     *
     * @return immutable list of school-type UI and API prefixes
     */
    public static List<String> organizationWritePrefixes() {
        return ORGANIZATION_WRITE_PREFIXES;
    }

    /**
     * Returns attendance prefixes reserved for the read-only Phase 4 vertical slice.
     *
     * @return immutable list of daily attendance UI and API prefixes
     */
    public static List<String> attendanceReadPrefixes() {
        return ATTENDANCE_READ_PREFIXES;
    }

    /**
     * Returns every request prefix that must have an explicit tenant route decision.
     *
     * @return immutable identity and organization prefix list
     */
    public static List<String> governedPrefixes() {
        return java.util.stream.Stream.of(
                        IDENTITY_READ_PREFIXES, ORGANIZATION_WRITE_PREFIXES, ATTENDANCE_READ_PREFIXES)
                .flatMap(List::stream)
                .toList();
    }

    /**
     * Builds the non-persistent fallback decisions used when the control plane is disabled.
     *
     * @return immutable AIS Next read-only decisions for every governed prefix
     */
    public static List<TenantRouteDecision> localSafeDecisions() {
        List<TenantRouteDecision> identity = IDENTITY_READ_PREFIXES.stream()
                .map(prefix -> new TenantRouteDecision(
                        "identity", prefix, RouteOwner.NEXT, WriteOwnership.NEXT_READ_ONLY, 0L))
                .toList();
        List<TenantRouteDecision> organization = ORGANIZATION_WRITE_PREFIXES.stream()
                .map(prefix -> new TenantRouteDecision(
                        "organization", prefix, RouteOwner.LEGACY, WriteOwnership.LEGACY_WRITE, 0L))
                .toList();
        List<TenantRouteDecision> attendance = ATTENDANCE_READ_PREFIXES.stream()
                .map(prefix -> new TenantRouteDecision(
                        "attendance", prefix, RouteOwner.LEGACY, WriteOwnership.LEGACY_WRITE, 0L))
                .toList();
        return java.util.stream.Stream.of(identity, organization, attendance)
                .flatMap(List::stream)
                .toList();
    }
}
