package id.aisnext.finance.web;

import id.aisnext.finance.api.AccountGroupEntry;
import id.aisnext.finance.application.AccountGroupService;
import id.aisnext.kernel.api.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Versioned JSON boundary for the data-minimized, read-only account-group directory. */
@RestController
@RequestMapping("/api/v1/finance/account-groups")
public class AccountGroupApiController {
    private final AccountGroupService service;

    /**
     * Creates the account-group API controller.
     *
     * @param service validated account-group application boundary
     */
    public AccountGroupApiController(AccountGroupService service) {
        this.service = service;
    }

    /**
     * Returns one deterministic page of global financial account groups.
     *
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param query literal account-group name fragment
     * @return immutable page containing only the three approved fields
     */
    @GetMapping
    public PageResult<AccountGroupEntry> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(name = "q", defaultValue = "") String query) {
        return service.findAccountGroups(page, size, query);
    }
}
