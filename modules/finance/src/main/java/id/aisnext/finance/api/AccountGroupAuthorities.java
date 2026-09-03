package id.aisnext.finance.api;

/** Stable Spring Security authorities derived from legacy accounting menu {@code 36332}. */
public final class AccountGroupAuthorities {
    /** Permission to browse the data-minimized account-group directory. */
    public static final String READ_ACCOUNT_GROUPS = "LEGACY_MENU_36332_READ";

    /** Prevents instantiation of this authority-name catalog. */
    private AccountGroupAuthorities() {
    }
}
