package id.aisnext.legacyfile.api;

import java.io.InputStream;
import java.util.Optional;

/**
 * Read-only boundary for streaming a legacy binary object without loading it fully into memory.
 */
public interface LegacyFileReadPort {
    /**
     * Opens a legacy file stream identified by a catalogued type and primary key.
     *
     * @param legacyType allowlisted legacy file category; never raw SQL or a table name from a client
     * @param legacyId legacy file identifier
     * @return an owned stream to be closed by the caller, or empty when the object does not exist
     */
    Optional<InputStream> open(String legacyType, long legacyId);
}
