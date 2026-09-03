package id.aisnext.security.api;

public interface ModernCredentialStore {
    void replaceAfterVerifiedLegacyLogin(String userId, String encodedPassword);
}
