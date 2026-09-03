package id.aisnext.supporting.infrastructure;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.supporting.api.LibraryItemTypeEntry;
import id.aisnext.supporting.domain.LibraryItemTypeRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * SELECT-only JDBC adapter for the data-minimized legacy library item-type projection.
 *
 * <p>The source relation is global and contains no tenant-scope column. Authorization therefore
 * remains at the request boundary, while this adapter enumerates approved fields and binds every
 * untrusted value.</p>
 */
@Repository
public class JdbcLibraryItemTypeRepository implements LibraryItemTypeRepository {
    private static final String FROM_AND_PREDICATE = """
              from library.jenis_item item_type
             where item_type.nama is not null
               and btrim(item_type.nama) <> ''
               and (lower(item_type.nama) like :pattern escape '\\'
                    or lower(coalesce(item_type.keterangan, '')) like :pattern escape '\\')
            """;

    private static final String SELECT_APPROVED_FIELDS = """
            select item_type.id item_type_id,
                   btrim(item_type.nama) item_type_name,
                   coalesce(btrim(item_type.keterangan), '') item_type_description
            """;

    private final JdbcClient core;

    /**
     * Creates the adapter with the tenant-routed CORE JDBC client.
     *
     * @param core tenant-aware projection client
     */
    public JdbcLibraryItemTypeRepository(@Qualifier("coreJdbcClient") JdbcClient core) {
        this.core = core;
    }

    /**
     * Counts and reads one deterministic page using the exact audited projection.
     *
     * @param query validated paging and literal text filter
     * @return global item-type page containing only approved fields
     */
    @Override
    public PageResult<LibraryItemTypeEntry> findItemTypes(PageQuery query) {
        String pattern = toLikePattern(query.filter());
        long total = core.sql("select count(*)" + FROM_AND_PREDICATE)
                .param("pattern", pattern)
                .query(Long.class)
                .single();
        List<LibraryItemTypeEntry> itemTypes = core.sql(SELECT_APPROVED_FIELDS
                        + FROM_AND_PREDICATE
                        + " order by item_type.nama asc, item_type.id asc"
                        + " limit :limit offset :offset")
                .param("pattern", pattern)
                .param("limit", query.size())
                .param("offset", query.offset())
                .query(this::map)
                .list();
        return new PageResult<>(itemTypes, query.page(), query.size(), total);
    }

    /**
     * Converts a user fragment to a lower-case literal SQL LIKE pattern.
     *
     * @param filter normalized filter supplied by {@link PageQuery}
     * @return escaped pattern whose percent and underscore characters remain literal
     */
    static String toLikePattern(String filter) {
        String escaped = filter.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    /**
     * Maps one JDBC row into the approved immutable response shape.
     *
     * @param resultSet current result row
     * @param rowNumber zero-based row number supplied by Spring JDBC
     * @return minimized library item-type entry
     * @throws SQLException when the JDBC driver cannot read an approved value
     */
    private LibraryItemTypeEntry map(ResultSet resultSet, int rowNumber) throws SQLException {
        return new LibraryItemTypeEntry(
                resultSet.getLong("item_type_id"),
                resultSet.getString("item_type_name"),
                resultSet.getString("item_type_description"));
    }
}
