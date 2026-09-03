package id.aisnext.academic.domain;

import id.aisnext.academic.api.StudentDirectoryEntry;
import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;

/** Read-only persistence port for the audited, role-scoped student projection. */
@FunctionalInterface
public interface StudentDirectoryRepository {
    /**
     * Returns active students inside the school and foundation scope of one active legacy role.
     *
     * @param activeRoleId exact role identifier used to derive row scope
     * @param query validated page and literal text filter
     * @return deterministic page of minimized student projections
     */
    PageResult<StudentDirectoryEntry> findVisibleActiveStudents(String activeRoleId, PageQuery query);
}
