package id.aisnext.organization.api;

import java.util.Locale;

/** Whitelisted deterministic sort options accepted by the school-type UI and API. */
public enum SchoolTypeSort {
    /** Sort by normalized name and then identifier, ascending. */
    NAME_ASC,
    /** Sort by normalized name and then identifier, descending. */
    NAME_DESC,
    /** Sort by legacy identifier ascending. */
    ID_ASC,
    /** Sort by legacy identifier descending. */
    ID_DESC;

    /**
     * Parses an external sort value without ever accepting a SQL fragment.
     *
     * @param value enum-style sort value; blank values select {@link #NAME_ASC}
     * @return recognized sort option
     * @throws IllegalArgumentException when the value is not supported
     */
    public static SchoolTypeSort parse(String value) {
        if (value == null || value.isBlank()) return NAME_ASC;
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
