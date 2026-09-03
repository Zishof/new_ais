package id.aisnext.identity.application;

import id.aisnext.legacycontract.api.LegacyIdentityQuery;
import id.aisnext.legacycontract.api.LegacyUserAccount;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Provides the credential-free legacy account projection used by the AIS Next profile screen.
 */
@Service
public class UserProfileService {
    private final LegacyIdentityQuery identities;

    /**
     * Creates the profile service over the read-only legacy identity port.
     *
     * @param identities active legacy account query
     */
    public UserProfileService(LegacyIdentityQuery identities) {
        this.identities = identities;
    }

    /**
     * Loads the active legacy account visible to the authenticated user.
     *
     * @param userId exact user identifier from the verified handoff principal
     * @return credential-free account projection, or empty if the account became inactive
     */
    public Optional<LegacyUserAccount> findActiveProfile(String userId) {
        return identities.findActiveUser(userId);
    }
}
