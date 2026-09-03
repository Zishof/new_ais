package id.aisnext.identity.web;

import static id.aisnext.identity.application.HandoffAuthorizationService.ROLE_DIRECTORY_READ_AUTHORITY;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Publishes server-authorized navigation visibility to every MVC model.
 *
 * <p>Hidden links are a usability aid only; matching endpoint authorization remains mandatory in
 * Spring Security and does not rely on this model attribute.</p>
 */
@ControllerAdvice
public class NavigationModelAdvice {
    /** Creates the stateless navigation model contributor. */
    public NavigationModelAdvice() {
    }

    /**
     * Reports whether the current session may read the legacy role directory.
     *
     * @param authentication current Spring Security authentication, or {@code null} when anonymous
     * @return {@code true} only when menu 2 grants the legacy read capability
     */
    @ModelAttribute("canReadRoleDirectory")
    public boolean canReadRoleDirectory(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_DIRECTORY_READ_AUTHORITY));
    }
}
