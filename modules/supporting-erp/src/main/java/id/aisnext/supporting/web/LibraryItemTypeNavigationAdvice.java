package id.aisnext.supporting.web;

import static id.aisnext.supporting.api.LibraryItemTypeAuthorities.READ_ITEM_TYPES;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Publishes the server-derived library item-type capability to Thymeleaf navigation. */
@ControllerAdvice
public class LibraryItemTypeNavigationAdvice {
    /** Creates the stateless library item-type capability contributor. */
    public LibraryItemTypeNavigationAdvice() {
    }

    /**
     * Reports whether the current active role can browse library item types.
     *
     * @param authentication current authentication, or null for anonymous traffic
     * @return whether legacy menu 56141 grants read access
     */
    @ModelAttribute("canReadLibraryItemTypes")
    public boolean canRead(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> READ_ITEM_TYPES.equals(authority.getAuthority()));
    }
}
