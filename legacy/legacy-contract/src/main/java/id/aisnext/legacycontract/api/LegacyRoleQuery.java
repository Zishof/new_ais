package id.aisnext.legacycontract.api;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import java.util.Optional;

public interface LegacyRoleQuery {
    PageResult<LegacyRoleSummary> findRoles(PageQuery query);
    Optional<LegacyRoleDetail> findRole(String roleId);
}
