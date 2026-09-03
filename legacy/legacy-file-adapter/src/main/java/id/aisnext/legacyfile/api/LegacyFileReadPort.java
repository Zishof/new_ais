package id.aisnext.legacyfile.api;

import java.io.InputStream;
import java.util.Optional;

public interface LegacyFileReadPort {
    Optional<InputStream> open(String legacyType, long legacyId);
}
