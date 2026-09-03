package id.aisnext.finance.domain;

import id.aisnext.finance.api.AccountGroupEntry;
import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;

/** Read-only persistence port for the audited global account-group projection. */
@FunctionalInterface
public interface AccountGroupRepository {
    /**
     * Returns one deterministic page containing only approved account-group fields.
     *
     * @param query validated page and literal name filter
     * @return page ordered by account-group name and legacy identifier
     */
    PageResult<AccountGroupEntry> findAccountGroups(PageQuery query);
}
