package id.aisnext.legacycontract.api;

import java.util.Optional;

/** Read-only identity projection port implemented against the immutable legacy schema contract. */
public interface LegacyIdentityQuery {
    /**
     * Finds an enabled legacy user and projects all assigned role identifiers.
     *
     * @param userId exact legacy user identifier
     * @return the active account, or empty when absent or disabled
     */
    Optional<LegacyUserAccount> findActiveUser(String userId);
}
