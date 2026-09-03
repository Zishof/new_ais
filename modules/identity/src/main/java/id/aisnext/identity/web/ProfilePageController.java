package id.aisnext.identity.web;

import id.aisnext.identity.application.UserProfileService;
import id.aisnext.security.api.HandoffPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * Renders the authenticated user's credential-free legacy account profile.
 */
@Controller
public class ProfilePageController {
    private final UserProfileService profiles;

    /**
     * Creates the server-rendered profile controller.
     *
     * @param profiles active legacy account profile service
     */
    public ProfilePageController(UserProfileService profiles) {
        this.profiles = profiles;
    }

    /**
     * Loads and renders the active account represented by the current handoff session.
     *
     * @param principal authenticated, tenant-bound handoff principal
     * @param model model receiving the credential-free account projection
     * @return {@code profile/index} Thymeleaf template name
     * @throws ResponseStatusException with HTTP 401 if the legacy account is no longer active
     */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal HandoffPrincipal principal, Model model) {
        var account = profiles.findActiveProfile(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Legacy user is no longer active"));
        model.addAttribute("account", account);
        model.addAttribute("activeRoleId", principal.activeRoleId());
        return "profile/index";
    }
}
