package id.aisnext.academic.application;

import id.aisnext.academic.api.StudentDirectoryEntry;
import id.aisnext.academic.domain.StudentDirectoryRepository;
import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application boundary for the read-only, role-scoped school-student directory. */
@Service
public class StudentDirectoryService {
    /** Maximum number of student rows accepted by one browser or API request. */
    public static final int MAXIMUM_PAGE_SIZE = 100;

    private final StudentDirectoryRepository repository;

    /**
     * Creates the directory service.
     *
     * @param repository tenant-aware, role-scoped student projection
     */
    public StudentDirectoryService(StudentDirectoryRepository repository) {
        this.repository = repository;
    }

    /**
     * Validates untrusted request bounds and returns one active-student page.
     *
     * @param activeRoleId exact active legacy role selected during handoff
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param filter case-insensitive literal student-name or student-number fragment
     * @return immutable page containing only fields approved by the Phase 5 contract
     * @throws NullPointerException when the active role identifier is absent
     * @throws IllegalArgumentException when the role is blank or paging is outside its bounds
     */
    @Transactional(transactionManager = "coreTransactionManager", readOnly = true)
    public PageResult<StudentDirectoryEntry> findStudents(String activeRoleId, int page, int size,
                                                           String filter) {
        Objects.requireNonNull(activeRoleId, "activeRoleId is required");
        if (activeRoleId.isBlank()) {
            throw new IllegalArgumentException("activeRoleId must not be blank");
        }
        if (size > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        return repository.findVisibleActiveStudents(activeRoleId, new PageQuery(page, size, filter));
    }
}
