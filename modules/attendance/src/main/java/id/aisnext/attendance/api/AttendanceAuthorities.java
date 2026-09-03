package id.aisnext.attendance.api;

/** Stable Spring Security authorities derived from legacy menu {@code 10000269}. */
public final class AttendanceAuthorities {
    /** Permission to browse the daily employee-attendance monitor. */
    public static final String READ_DAILY = "LEGACY_MENU_10000269_READ";

    /** Prevents instantiation of this authority-name catalog. */
    private AttendanceAuthorities() {
    }
}
