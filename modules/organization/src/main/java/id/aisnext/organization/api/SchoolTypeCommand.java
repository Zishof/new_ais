package id.aisnext.organization.api;

/**
 * Submitted mutable fields for a school-type create or update command.
 *
 * @param name required catalogue name
 * @param levelId required {@code public.jenjang} identifier
 * @param description optional description
 * @param active desired active state
 */
public record SchoolTypeCommand(String name, Long levelId, String description, boolean active) {
    /** Creates the unvalidated command payload received by the application boundary. */
    public SchoolTypeCommand {
    }
}
