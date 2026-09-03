package id.aisnext.academic.api;

/** Stable Spring Security authorities derived from legacy student menu {@code 887727}. */
public final class StudentDirectoryAuthorities {
    /** Permission to browse the minimized school-student directory. */
    public static final String READ_STUDENTS = "LEGACY_MENU_887727_READ";

    /** Prevents instantiation of this authority-name catalog. */
    private StudentDirectoryAuthorities() {
    }
}
