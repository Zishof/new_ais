package id.aisnext.security.api;

/** Write boundary for gradual password rehash after a verified legacy login. */
public interface ModernCredentialStore {
    /**
     * Replaces or creates the modern hash only after legacy verification has succeeded.
     *
     * @param userId exact user identifier associated with the verified login
     * @param encodedPassword modern, salted password encoding; never plaintext
     */
    void replaceAfterVerifiedLegacyLogin(String userId, String encodedPassword);
}
