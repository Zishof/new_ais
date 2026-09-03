package id.aisnext.academic.api;

/**
 * Immutable, data-minimized view of one school student visible to the active legacy role.
 *
 * @param studentId legacy student primary key
 * @param studentNumber student number used by the legacy directory
 * @param studentName student display name
 * @param entryYear required legacy entry year
 * @param schoolName school display name
 * @param currentClassName current class display name, or blank when unassigned
 * @param initialStatusName initial-status display name, or blank when absent
 * @param exitStatusName exit-status display name, or blank when the student has not exited
 * @param active legacy null-active values normalized to true
 */
public record StudentDirectoryEntry(long studentId, String studentNumber, String studentName,
                                    int entryYear, String schoolName, String currentClassName,
                                    String initialStatusName, String exitStatusName,
                                    boolean active) {
}
