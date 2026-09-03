package id.aisnext.organization.api;

import java.time.LocalDateTime;

/**
 * Credential-free projection of {@code sekolah.jenis_sekolah} and its level label.
 *
 * @param id legacy primary key
 * @param name school-type name used by reports and public pages
 * @param levelId referenced legacy education-level identifier
 * @param levelName referenced level display label
 * @param description optional operator description
 * @param active null-compatible active state normalized to a primitive boolean
 * @param changedBy display identity of the last writer, when present
 * @param changedById stable identity of the last writer, when present
 * @param changedAt legacy local timestamp of the latest change, when present
 * @param versionToken deterministic snapshot token required by update and delete commands
 */
public record SchoolType(long id, String name, Long levelId, String levelName, String description,
                         boolean active, String changedBy, String changedById,
                         LocalDateTime changedAt, String versionToken) {
    /** Creates the immutable school-type projection. */
    public SchoolType {
    }
}
