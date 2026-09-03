package id.aisnext.legacycontract.api;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import java.util.Optional;

/** Read-only RBAC projection port implemented without modifying the legacy authorization schema. */
public interface LegacyRoleQuery {
    /**
     * Searches legacy roles using deterministic server-side pagination.
     *
     * @param query validated page, size, and text filter
     * @return one page of role summaries and the matching total
     */
    PageResult<LegacyRoleSummary> findRoles(PageQuery query);

    /**
     * Loads one role and its effective legacy menu privileges.
     *
     * @param roleId exact legacy role identifier
     * @return role details, or empty when no role matches
     */
    Optional<LegacyRoleDetail> findRole(String roleId);
}
