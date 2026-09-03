package id.aisnext.legacycontract.api;

import java.util.Optional;

public interface LegacyIdentityQuery {
    Optional<LegacyUserAccount> findActiveUser(String userId);
}
