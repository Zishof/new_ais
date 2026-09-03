package id.aisnext.organization.api;

/** Stable Spring Security authorities derived from legacy menu {@code 881247}. */
public final class SchoolTypeAuthorities {
    /** Permission to list, inspect, and export school types. */
    public static final String READ = "LEGACY_MENU_881247_READ";
    /** Permission to create a school type. */
    public static final String CREATE = "LEGACY_MENU_881247_CREATE";
    /** Permission to change an existing school type. */
    public static final String UPDATE = "LEGACY_MENU_881247_UPDATE";
    /** Permission to delete an unreferenced school type. */
    public static final String DELETE = "LEGACY_MENU_881247_DELETE";

    /** Prevents instantiation of this authority-name catalog. */
    private SchoolTypeAuthorities() {
    }
}
