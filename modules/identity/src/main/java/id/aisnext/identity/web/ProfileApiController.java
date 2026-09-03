package id.aisnext.identity.web;

import id.aisnext.identity.application.UserProfileService;
import id.aisnext.legacycontract.api.LegacyUserAccount;
import id.aisnext.security.api.HandoffPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Versioned REST endpoint for the authenticated user's credential-free legacy profile.
 */
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileApiController {
    private final UserProfileService profiles;

    /**
     * Creates the profile API controller.
     *
     * @param profiles active legacy account profile service
     */
    public ProfileApiController(UserProfileService profiles) {
        this.profiles = profiles;
    }

    /**
     * Returns the active account associated with the current handoff session.
     *
     * @param principal authenticated, tenant-bound handoff principal
     * @return credential-free active legacy account projection
     * @throws ResponseStatusException with HTTP 401 if the legacy account is no longer active
     */
    @GetMapping
    public LegacyUserAccount profile(@AuthenticationPrincipal HandoffPrincipal principal) {
        return profiles.findActiveProfile(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Legacy user is no longer active"));
    }
}
