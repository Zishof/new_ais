package id.aisnext.organization.web;

import id.aisnext.organization.api.SchoolTypeCommand;

/**
 * JSON request body for create and update operations.
 *
 * @param name required school-type name
 * @param levelId required legacy education-level identifier
 * @param description optional description
 * @param active optional active state; omitted values default to active for legacy compatibility
 */
public record SchoolTypeRequest(String name, Long levelId, String description, Boolean active) {
    /** Creates the untrusted transport request. */
    public SchoolTypeRequest {
    }

    /**
     * Converts transport nullability into the application command contract.
     *
     * @return command whose absent active value is normalized to {@code true}
     */
    public SchoolTypeCommand toCommand() {
        return new SchoolTypeCommand(name, levelId, description, active == null || active);
    }
}
