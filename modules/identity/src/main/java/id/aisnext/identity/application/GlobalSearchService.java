package id.aisnext.identity.application;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Aggregates authorized search providers for capabilities already migrated to AIS Next.
 *
 * <p>The initial provider searches the read-only legacy role directory. Additional modules can
 * contribute results after their own privilege and tenant-isolation contracts are proven.</p>
 */
@Service
public class GlobalSearchService {
    private static final int RESULT_LIMIT = 10;
    private final RoleDirectoryService roles;

    /**
     * Creates global search with the currently migrated role-directory provider.
     *
     * @param roles read-only role directory service
     */
    public GlobalSearchService(RoleDirectoryService roles) {
        this.roles = roles;
    }

    /**
     * Searches authorized role summaries using a bounded server-side result set.
     *
     * @param query user-entered query; blank or one-character values produce no results
     * @return at most ten navigation-safe search results
     */
    public List<SearchResult> search(String query) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.length() < 2) {
            return List.of();
        }
        return roles.find(0, RESULT_LIMIT, normalized).items().stream()
                .map(role -> new SearchResult(
                        "Grup pengguna",
                        role.id(),
                        role.name(),
                        "/roles?q=" + URLEncoder.encode(role.id(), StandardCharsets.UTF_8)))
                .toList();
    }
}
