package id.aisnext.security.api;

public interface LegacyCredentialVerifier {
    boolean matches(String userId, char[] presentedPassword);
}
