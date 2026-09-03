package id.aisnext.organization.web;

import static id.aisnext.organization.api.SchoolTypeAuthorities.CREATE;
import static id.aisnext.organization.api.SchoolTypeAuthorities.DELETE;
import static id.aisnext.organization.api.SchoolTypeAuthorities.READ;
import static id.aisnext.organization.api.SchoolTypeAuthorities.UPDATE;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Publishes server-derived school-type capabilities to Thymeleaf navigation and forms. */
@ControllerAdvice
public class SchoolTypeNavigationAdvice {
    /** Creates the stateless capability model contributor. */
    public SchoolTypeNavigationAdvice() {
    }

    /**
     * Reports whether the current role can browse school types.
     *
     * @param authentication current authentication, or null for anonymous traffic
     * @return whether legacy menu 881247 grants READ
     */
    @ModelAttribute("canReadSchoolTypes")
    public boolean canRead(Authentication authentication) {
        return has(authentication, READ);
    }

    /**
     * Reports whether the current role can create school types.
     *
     * @param authentication current authentication, or null for anonymous traffic
     * @return whether legacy menu 881247 grants CREATE
     */
    @ModelAttribute("canCreateSchoolTypes")
    public boolean canCreate(Authentication authentication) {
        return has(authentication, CREATE);
    }

    /**
     * Reports whether the current role can update school types.
     *
     * @param authentication current authentication, or null for anonymous traffic
     * @return whether legacy menu 881247 grants UPDATE
     */
    @ModelAttribute("canUpdateSchoolTypes")
    public boolean canUpdate(Authentication authentication) {
        return has(authentication, UPDATE);
    }

    /**
     * Reports whether the current role can delete school types.
     *
     * @param authentication current authentication, or null for anonymous traffic
     * @return whether legacy menu 881247 grants DELETE
     */
    @ModelAttribute("canDeleteSchoolTypes")
    public boolean canDelete(Authentication authentication) {
        return has(authentication, DELETE);
    }

    /**
     * Checks one exact authority without relying on hidden-link authorization.
     *
     * @param authentication current authentication
     * @param expected exact authority string
     * @return whether the authenticated role contains the authority
     */
    private static boolean has(Authentication authentication, String expected) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(expected));
    }
}
