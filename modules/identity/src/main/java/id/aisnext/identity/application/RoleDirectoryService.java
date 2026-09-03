package id.aisnext.identity.application;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.legacycontract.api.LegacyRoleDetail;
import id.aisnext.legacycontract.api.LegacyRoleQuery;
import id.aisnext.legacycontract.api.LegacyRoleSummary;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Application service exposing the first read-only identity/RBAC vertical slice.
 *
 * <p>The service validates page input through {@link PageQuery} and delegates only to the legacy
 * read port; it owns no legacy write path.</p>
 */
@Service
public class RoleDirectoryService {
    private final LegacyRoleQuery roles;

    /**
     * Creates the role directory service.
     *
     * @param roles read-only legacy role port
     */
    public RoleDirectoryService(LegacyRoleQuery roles) { this.roles = roles; }

    /**
     * Finds a filtered page of legacy roles.
     *
     * @param page zero-based page index
     * @param size requested number of records, subject to {@link PageQuery} limits
     * @param filter case-insensitive identifier/name filter; {@code null} becomes empty
     * @return page of role summaries and matching total
     * @throws IllegalArgumentException when page or size violates the API contract
     */
    public PageResult<LegacyRoleSummary> find(int page, int size, String filter) {
        return roles.findRoles(new PageQuery(page, size, filter));
    }

    /**
     * Finds one legacy role and its effective menu privileges.
     *
     * @param roleId exact legacy role identifier
     * @return role detail, or empty when not found
     */
    public Optional<LegacyRoleDetail> findOne(String roleId) { return roles.findRole(roleId); }
}
