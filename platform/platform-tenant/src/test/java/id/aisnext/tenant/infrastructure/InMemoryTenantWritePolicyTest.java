package id.aisnext.tenant.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantWriteDecision;
import id.aisnext.tenant.api.WriteOwnership;
import id.aisnext.tenant.api.WriteOwnershipDeniedException;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Verifies fail-closed aggregate ownership behavior without a control database. */
class InMemoryTenantWritePolicyTest {
    private static final TenantId TENANT = new TenantId("tenant-a");
    private static final String AGGREGATE = "organization.jenis-sekolah";

    /** Creates the test fixture container. */
    InMemoryTenantWritePolicyTest() {
    }

    /** Confirms that an exact {@code NEXT_WRITE} decision authorizes the command. */
    @Test
    void allowsOnlyExactNextWriteDecision() {
        TenantWriteDecision decision = new TenantWriteDecision(AGGREGATE, WriteOwnership.NEXT_WRITE, 3L);
        var policy = new InMemoryTenantWritePolicy(Map.of(TENANT, Map.of(AGGREGATE, decision)));

        assertThat(policy.requireNextWrite(TENANT, AGGREGATE)).isEqualTo(decision);
    }

    /** Confirms that absent and legacy-owned aggregates are denied instead of defaulting open. */
    @Test
    void deniesMissingAndLegacyOwnedDecisions() {
        var missing = new InMemoryTenantWritePolicy(Map.of());
        var legacy = new InMemoryTenantWritePolicy(Map.of(TENANT, Map.of(AGGREGATE,
                new TenantWriteDecision(AGGREGATE, WriteOwnership.LEGACY_WRITE, 0L))));

        assertThatThrownBy(() -> missing.requireNextWrite(TENANT, AGGREGATE))
                .isInstanceOf(WriteOwnershipDeniedException.class);
        assertThatThrownBy(() -> legacy.requireNextWrite(TENANT, AGGREGATE))
                .isInstanceOf(WriteOwnershipDeniedException.class);
    }
}
