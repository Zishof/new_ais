package id.aisnext.config;

import static org.assertj.core.api.Assertions.assertThat;

import id.aisnext.tenant.api.RouteOwner;
import id.aisnext.tenant.api.TenantRouteDecision;
import id.aisnext.tenant.api.WriteOwnership;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Verifies safe fallback ownership for every migration-phase route prefix. */
class PhaseTwoRoutesTest {
    /** Creates the route catalog test fixture. */
    PhaseTwoRoutesTest() {
    }

    /** Confirms both attendance prefixes are governed and remain legacy-owned by default. */
    @Test
    void keepsAttendanceLegacyOwnedInSafeFallback() {
        assertThat(PhaseTwoRoutes.attendanceReadPrefixes())
                .containsExactly("/attendance/daily", "/api/v1/attendance/daily");
        assertThat(PhaseTwoRoutes.governedPrefixes())
                .containsAll(PhaseTwoRoutes.attendanceReadPrefixes());

        List<TenantRouteDecision> attendance = PhaseTwoRoutes.localSafeDecisions().stream()
                .filter(decision -> decision.moduleKey().equals("attendance"))
                .toList();
        assertThat(attendance).hasSize(2).allSatisfy(decision -> {
            assertThat(decision.owner()).isEqualTo(RouteOwner.LEGACY);
            assertThat(decision.writeOwnership()).isEqualTo(WriteOwnership.LEGACY_WRITE);
        });
    }

    /** Confirms both student-directory prefixes are governed and legacy-owned by default. */
    @Test
    void keepsAcademicDirectoryLegacyOwnedInSafeFallback() {
        assertThat(PhaseTwoRoutes.academicReadPrefixes())
                .containsExactly("/academic/students", "/api/v1/academic/students");
        assertThat(PhaseTwoRoutes.governedPrefixes())
                .containsAll(PhaseTwoRoutes.academicReadPrefixes());

        List<TenantRouteDecision> academic = PhaseTwoRoutes.localSafeDecisions().stream()
                .filter(decision -> decision.moduleKey().equals("academic-core"))
                .toList();
        assertThat(academic).hasSize(2).allSatisfy(decision -> {
            assertThat(decision.owner()).isEqualTo(RouteOwner.LEGACY);
            assertThat(decision.writeOwnership()).isEqualTo(WriteOwnership.LEGACY_WRITE);
        });
    }
}
