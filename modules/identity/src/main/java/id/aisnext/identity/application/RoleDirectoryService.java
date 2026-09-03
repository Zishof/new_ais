package id.aisnext.identity.application;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.legacycontract.api.LegacyRoleDetail;
import id.aisnext.legacycontract.api.LegacyRoleQuery;
import id.aisnext.legacycontract.api.LegacyRoleSummary;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RoleDirectoryService {
    private final LegacyRoleQuery roles;
    public RoleDirectoryService(LegacyRoleQuery roles) { this.roles = roles; }
    public PageResult<LegacyRoleSummary> find(int page, int size, String filter) {
        return roles.findRoles(new PageQuery(page, size, filter));
    }
    public Optional<LegacyRoleDetail> findOne(String roleId) { return roles.findRole(roleId); }
}
