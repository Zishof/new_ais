package id.aisnext.finance.web;

import static id.aisnext.finance.api.AccountGroupAuthorities.READ_ACCOUNT_GROUPS;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Publishes the server-derived account-group capability to Thymeleaf navigation. */
@ControllerAdvice
public class AccountGroupNavigationAdvice {
    /** Creates the stateless account-group capability contributor. */
    public AccountGroupNavigationAdvice() {
    }

    /**
     * Reports whether the current active role can browse account groups.
     *
     * @param authentication current authentication, or null for anonymous traffic
     * @return whether legacy menu 36332 grants read access
     */
    @ModelAttribute("canReadAccountGroups")
    public boolean canRead(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> READ_ACCOUNT_GROUPS.equals(authority.getAuthority()));
    }
}
