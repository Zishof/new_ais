package id.aisnext.organization.domain;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.organization.api.SchoolLevel;
import id.aisnext.organization.api.SchoolType;
import id.aisnext.organization.api.SchoolTypeCommand;
import id.aisnext.organization.api.SchoolTypeSort;
import java.util.List;
import java.util.Optional;

/** Persistence boundary for the exact legacy school-type and Envers contracts. */
public interface SchoolTypeRepository {
    /**
     * Returns a filtered, sorted, server-paged catalogue projection.
     *
     * @param query validated paging and name-filter parameters
     * @param sort whitelisted deterministic sort order
     * @param activeOnly whether to include only null-compatible active rows
     * @return page of school-type projections and matching total
     */
    PageResult<SchoolType> findAll(PageQuery query, SchoolTypeSort sort, boolean activeOnly);

    /**
     * Finds one school type without locking it.
     *
     * @param id legacy primary key
     * @return projection, or empty when absent
     */
    Optional<SchoolType> findById(long id);

    /**
     * Returns active levels plus inactive levels already referenced by a school type.
     *
     * @return deterministic level choices safe for legacy data editing
     */
    List<SchoolLevel> findSelectableLevels();

    /**
     * Reports whether a submitted level identifier exists.
     *
     * @param levelId legacy level primary key
     * @return {@code true} when the foreign-key target exists
     */
    boolean levelExists(long levelId);

    /**
     * Reports whether another row has the same exact normalized name.
     *
     * @param name trimmed, case-sensitive name
     * @param excludedId row to exclude during update, or {@code null} during create
     * @return {@code true} when a conflicting row exists
     */
    boolean nameExists(String name, Long excludedId);

    /**
     * Creates a business row and its Envers-compatible audit snapshot in one transaction.
     *
     * @param command validated command
     * @param actorId authenticated legacy user identifier
     * @return persisted projection
     */
    SchoolType create(SchoolTypeCommand command, String actorId);

    /**
     * Locks and updates a row only when its submitted snapshot token remains current.
     *
     * @param id legacy primary key
     * @param versionToken expected current row token
     * @param command validated command
     * @param actorId authenticated legacy user identifier
     * @return updated projection
     */
    SchoolType update(long id, String versionToken, SchoolTypeCommand command, String actorId);

    /**
     * Locks and deletes an unreferenced row when its submitted snapshot token remains current.
     *
     * @param id legacy primary key
     * @param versionToken expected current row token
     * @param actorId authenticated legacy user identifier
     */
    void delete(long id, String versionToken, String actorId);
}
