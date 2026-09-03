package id.aisnext.security.api;

/**
 * Transitional boundary for validating a presented password with the current legacy mechanism.
 *
 * <p>Implementations must not log, persist, or retain the presented character array.</p>
 */
public interface LegacyCredentialVerifier {
    /**
     * Validates credentials without exposing the stored legacy representation.
     *
     * @param userId exact legacy user identifier
     * @param presentedPassword mutable caller-owned password characters to clear after use
     * @return whether the supplied password is valid for the active account
     */
    boolean matches(String userId, char[] presentedPassword);
}
