package id.aisnext.organization.infrastructure;

import id.aisnext.organization.api.SchoolType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/** Computes opaque deterministic concurrency tokens without adding a column to the legacy table. */
final class SchoolTypeVersion {
    /** Prevents instantiation of this stateless hashing utility. */
    private SchoolTypeVersion() {
    }

    /**
     * Hashes every mutable physical column represented by a school-type projection.
     *
     * @param value row snapshot whose token is required
     * @return unpadded URL-safe SHA-256 token
     * @throws IllegalStateException when the required JDK SHA-256 implementation is unavailable
     */
    static String token(SchoolType value) {
        String canonical = String.join("\u001f",
                Long.toString(value.id()), value.name(), text(value.description()),
                value.levelId() == null ? "" : value.levelId().toString(),
                Boolean.toString(value.active()), text(value.changedBy()), text(value.changedById()),
                value.changedAt() == null ? "" : value.changedAt().toString());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the Java platform", exception);
        }
    }

    /**
     * Normalizes nullable database text for canonical token generation.
     *
     * @param value nullable text
     * @return empty text for null, otherwise the original value
     */
    private static String text(String value) {
        return value == null ? "" : value;
    }
}
