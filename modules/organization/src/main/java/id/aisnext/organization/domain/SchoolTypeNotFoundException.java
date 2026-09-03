package id.aisnext.organization.domain;

/** Raised when a command or detail request targets a missing legacy school-type identifier. */
public final class SchoolTypeNotFoundException extends RuntimeException {
    /**
     * Creates a not-found failure for the specified identifier.
     *
     * @param id missing legacy primary key
     */
    public SchoolTypeNotFoundException(long id) {
        super("Jenis sekolah " + id + " tidak ditemukan");
    }
}
