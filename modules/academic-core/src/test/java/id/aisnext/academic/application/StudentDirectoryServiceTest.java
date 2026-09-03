package id.aisnext.academic.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import id.aisnext.academic.domain.StudentDirectoryRepository;
import id.aisnext.kernel.api.PageQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Verifies validation and role propagation at the student-directory application boundary. */
class StudentDirectoryServiceTest {
    private final StudentDirectoryRepository repository = mock(StudentDirectoryRepository.class);
    private final StudentDirectoryService service = new StudentDirectoryService(repository);

    /** Creates the service test fixture. */
    StudentDirectoryServiceTest() {
    }

    /** Confirms the exact active role and normalized page query reach the persistence port. */
    @Test
    void delegatesRoleScopedRequest() {
        service.findStudents("amp", 2, 25, "  UAT-01  ");

        ArgumentCaptor<PageQuery> query = ArgumentCaptor.forClass(PageQuery.class);
        verify(repository).findVisibleActiveStudents(org.mockito.ArgumentMatchers.eq("amp"),
                query.capture());
        assertThat(query.getValue()).isEqualTo(new PageQuery(2, 25, "UAT-01"));
    }

    /** Confirms an oversized response is rejected before repository access. */
    @Test
    void rejectsOversizedPage() {
        assertThatThrownBy(() -> service.findStudents("amp", 0, 101, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("size must be between 1 and 100");
    }

    /** Confirms a missing role cannot accidentally create an unscoped directory query. */
    @Test
    void rejectsMissingRole() {
        assertThatThrownBy(() -> service.findStudents(null, 0, 25, ""))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("activeRoleId is required");
    }

    /** Confirms a blank role cannot accidentally create an unscoped directory query. */
    @Test
    void rejectsBlankRole() {
        assertThatThrownBy(() -> service.findStudents("  ", 0, 25, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("activeRoleId must not be blank");
    }
}
