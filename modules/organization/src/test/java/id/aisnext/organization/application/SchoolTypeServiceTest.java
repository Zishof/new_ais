package id.aisnext.organization.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.aisnext.organization.api.SchoolType;
import id.aisnext.organization.api.SchoolTypeCommand;
import id.aisnext.organization.domain.SchoolTypeConflictException;
import id.aisnext.organization.domain.SchoolTypeRepository;
import id.aisnext.organization.domain.SchoolTypeValidationException;
import id.aisnext.tenant.api.ResolvedTenant;
import id.aisnext.tenant.api.TenantContext;
import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantMode;
import id.aisnext.tenant.api.TenantWriteDecision;
import id.aisnext.tenant.api.WriteOwnership;
import id.aisnext.tenant.api.WriteOwnershipDeniedException;
import id.aisnext.tenant.infrastructure.InMemoryTenantWritePolicy;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Verifies command validation and fail-closed ownership before JDBC mutation. */
class SchoolTypeServiceTest {
    private static final TenantId TENANT_ID = new TenantId("uat-local");
    private static final ResolvedTenant TENANT = new ResolvedTenant(
            TENANT_ID, "UAT", TenantMode.HYBRID, Locale.forLanguageTag("id-ID"), ZoneId.of("Asia/Jakarta"));

    /** Creates the test fixture container. */
    SchoolTypeServiceTest() {
    }

    /** Confirms that normalized valid fields reach the repository under {@code NEXT_WRITE}. */
    @Test
    void createsNormalizedSchoolTypeWhenNextOwnsWrites() {
        SchoolTypeRepository repository = mock(SchoolTypeRepository.class);
        SchoolTypeCommand normalized = new SchoolTypeCommand("SMA Baru", 33L, null, true);
        SchoolType created = new SchoolType(7L, "SMA Baru", 33L, "SMA", null,
                true, "admin", "admin", null, "version");
        when(repository.levelExists(33L)).thenReturn(true);
        when(repository.nameExists("SMA Baru", null)).thenReturn(false);
        when(repository.create(normalized, "admin")).thenReturn(created);

        try (TenantContext.Scope ignored = TenantContext.open(TENANT)) {
            SchoolTypeService service = new SchoolTypeService(repository, nextWritePolicy());
            assertThat(service.create(new SchoolTypeCommand("  SMA Baru  ", 33L, "  ", true), " admin "))
                    .isEqualTo(created);
        }
        verify(repository).create(normalized, "admin");
    }

    /** Confirms missing ownership denies the command before validation or repository mutation. */
    @Test
    void deniesCommandWhenOwnershipMetadataIsMissing() {
        SchoolTypeRepository repository = mock(SchoolTypeRepository.class);
        SchoolTypeService service = new SchoolTypeService(repository, new InMemoryTenantWritePolicy(Map.of()));

        try (TenantContext.Scope ignored = TenantContext.open(TENANT)) {
            assertThatThrownBy(() -> service.create(
                    new SchoolTypeCommand("SMA Baru", 33L, null, true), "admin"))
                    .isInstanceOf(WriteOwnershipDeniedException.class);
        }
        verify(repository, never()).create(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString());
    }

    /** Confirms blank names and duplicate exact names produce distinct safe failures. */
    @Test
    void validatesRequiredAndUniqueName() {
        SchoolTypeRepository repository = mock(SchoolTypeRepository.class);
        when(repository.levelExists(33L)).thenReturn(true);
        when(repository.nameExists("SMA", null)).thenReturn(true);
        SchoolTypeService service = new SchoolTypeService(repository, nextWritePolicy());

        try (TenantContext.Scope ignored = TenantContext.open(TENANT)) {
            assertThatThrownBy(() -> service.create(
                    new SchoolTypeCommand(" ", 33L, null, true), "admin"))
                    .isInstanceOf(SchoolTypeValidationException.class);
            assertThatThrownBy(() -> service.create(
                    new SchoolTypeCommand("SMA", 33L, null, true), "admin"))
                    .isInstanceOf(SchoolTypeConflictException.class);
        }
    }

    /**
     * Builds a single-tenant policy that authorizes only the audited school-type aggregate.
     *
     * @return deterministic {@code NEXT_WRITE} fixture policy
     */
    private static InMemoryTenantWritePolicy nextWritePolicy() {
        TenantWriteDecision decision = new TenantWriteDecision(
                SchoolTypeService.AGGREGATE_KEY, WriteOwnership.NEXT_WRITE, 1L);
        return new InMemoryTenantWritePolicy(Map.of(
                TENANT_ID, Map.of(SchoolTypeService.AGGREGATE_KEY, decision)));
    }
}
