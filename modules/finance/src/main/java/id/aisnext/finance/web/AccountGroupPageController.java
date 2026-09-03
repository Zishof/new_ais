package id.aisnext.finance.web;

import id.aisnext.finance.application.AccountGroupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Accessible server-rendered boundary for the minimized account-group directory. */
@Controller
public class AccountGroupPageController {
    private final AccountGroupService service;

    /**
     * Creates the account-group page controller.
     *
     * @param service validated account-group application boundary
     */
    public AccountGroupPageController(AccountGroupService service) {
        this.service = service;
    }

    /**
     * Renders one account-group page while preserving its filter and paging state.
     *
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param query literal account-group name fragment
     * @param model template model receiving the page and preserved filter
     * @return account-group directory template name
     */
    @GetMapping("/finance/account-groups")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "25") int size,
                       @RequestParam(name = "q", defaultValue = "") String query,
                       Model model) {
        model.addAttribute("result", service.findAccountGroups(page, size, query));
        model.addAttribute("query", query == null ? "" : query.trim());
        return "finance/account-groups";
    }
}
