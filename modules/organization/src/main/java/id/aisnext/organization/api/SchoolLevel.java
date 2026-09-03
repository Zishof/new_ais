package id.aisnext.organization.api;

/**
 * Read projection of a legacy education level available to a school type.
 *
 * @param id legacy {@code public.jenjang} primary key
 * @param name display label
 * @param active explicit or null-compatible active state
 */
public record SchoolLevel(long id, String name, boolean active) {
    /** Creates the immutable level projection. */
    public SchoolLevel {
    }
}
