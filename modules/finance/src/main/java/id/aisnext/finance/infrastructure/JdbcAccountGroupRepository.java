package id.aisnext.finance.infrastructure;

import id.aisnext.finance.api.AccountGroupEntry;
import id.aisnext.finance.domain.AccountGroupRepository;
import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * SELECT-only JDBC adapter for the data-minimized legacy account-group projection.
 *
 * <p>The relation is global and contains no tenant-scope column. Authorization remains at the
 * request boundary, while this adapter enumerates approved fields and binds every untrusted
 * filter value.</p>
 */
@Repository
public class JdbcAccountGroupRepository implements AccountGroupRepository {
    private static final String FROM_AND_PREDICATE = """
              from akunting.grup_akun account_group
             where account_group.nama is not null
               and btrim(account_group.nama) <> ''
               and lower(account_group.nama) like :pattern escape '\\'
            """;

    private static final String SELECT_APPROVED_FIELDS = """
            select account_group.id account_group_id,
                   btrim(account_group.nama) account_group_name,
                   coalesce(btrim(account_group.keterangan), '') account_group_description
            """;

    private final JdbcClient core;

    /**
     * Creates the adapter with the tenant-routed CORE JDBC client.
     *
     * @param core tenant-aware projection client
     */
    public JdbcAccountGroupRepository(@Qualifier("coreJdbcClient") JdbcClient core) {
        this.core = core;
    }

    /**
     * Counts and reads one deterministic page using the exact audited projection.
     *
     * @param query validated paging and literal name filter
     * @return global account-group page containing only approved fields
     */
    @Override
    public PageResult<AccountGroupEntry> findAccountGroups(PageQuery query) {
        String pattern = toLikePattern(query.filter());
        long total = core.sql("select count(*)" + FROM_AND_PREDICATE)
                .param("pattern", pattern)
                .query(Long.class)
                .single();
        List<AccountGroupEntry> accountGroups = core.sql(SELECT_APPROVED_FIELDS
                        + FROM_AND_PREDICATE
                        + " order by account_group.nama asc, account_group.id asc"
                        + " limit :limit offset :offset")
                .param("pattern", pattern)
                .param("limit", query.size())
                .param("offset", query.offset())
                .query(this::map)
                .list();
        return new PageResult<>(accountGroups, query.page(), query.size(), total);
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
     * @return minimized account-group entry
     * @throws SQLException when the JDBC driver cannot read an approved value
     */
    private AccountGroupEntry map(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AccountGroupEntry(
                resultSet.getLong("account_group_id"),
                resultSet.getString("account_group_name"),
                resultSet.getString("account_group_description"));
    }
}
