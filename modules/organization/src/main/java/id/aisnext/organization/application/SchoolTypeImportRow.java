package id.aisnext.organization.application;

import id.aisnext.organization.api.SchoolTypeCommand;

/**
 * One validated-shape workbook row before database-level validation.
 *
 * @param id existing legacy identifier to update, or null to create
 * @param versionToken exported concurrency token required for an update
 * @param command submitted mutable fields
 */
public record SchoolTypeImportRow(Long id, String versionToken, SchoolTypeCommand command) {
    /** Creates the immutable imported row. */
    public SchoolTypeImportRow {
    }
}
