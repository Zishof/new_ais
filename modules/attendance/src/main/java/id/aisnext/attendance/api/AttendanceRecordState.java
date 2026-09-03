package id.aisnext.attendance.api;

import java.util.Locale;

/** Closed filter and response vocabulary for the presence of a daily attendance row. */
public enum AttendanceRecordState {
    /** Includes employees regardless of whether the selected date has a row. */
    ALL,
    /** Includes only employees with at least one row on the selected date. */
    RECORDED,
    /** Includes only employees without a row on the selected date. */
    UNRECORDED;

    /**
     * Parses an untrusted request value without permitting SQL or vocabulary expansion.
     *
     * @param value requested state; null or blank means {@link #ALL}
     * @return one of the three supported states
     * @throws IllegalArgumentException when the value is not supported
     */
    public static AttendanceRecordState parse(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "recordState must be ALL, RECORDED, or UNRECORDED", exception);
        }
    }
}
