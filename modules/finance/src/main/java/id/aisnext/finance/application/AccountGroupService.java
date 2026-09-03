package id.aisnext.finance.application;

import id.aisnext.finance.api.AccountGroupEntry;
import id.aisnext.finance.domain.AccountGroupRepository;
import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application boundary for the read-only global account-group directory. */
@Service
public class AccountGroupService {
    /** Maximum number of account-group rows accepted by one browser or API request. */
    public static final int MAXIMUM_PAGE_SIZE = 100;

    private final AccountGroupRepository repository;

    /**
     * Creates the account-group directory service.
     *
     * @param repository tenant-aware, read-only account-group projection
     */
    public AccountGroupService(AccountGroupRepository repository) {
        this.repository = repository;
    }

    /**
     * Validates untrusted request bounds and returns one deterministic account-group page.
     *
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param filter case-insensitive literal account-group name fragment
     * @return immutable page containing only fields approved by the Phase 7 contract
     * @throws IllegalArgumentException when paging is outside its bounds
     */
    @Transactional(transactionManager = "coreTransactionManager", readOnly = true)
    public PageResult<AccountGroupEntry> findAccountGroups(int page, int size, String filter) {
        if (size > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        return repository.findAccountGroups(new PageQuery(page, size, filter));
    }
}
