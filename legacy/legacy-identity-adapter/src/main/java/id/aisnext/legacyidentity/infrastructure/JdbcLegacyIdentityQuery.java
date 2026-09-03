package id.aisnext.legacyidentity.infrastructure;

import id.aisnext.legacycontract.api.LegacyIdentityQuery;
import id.aisnext.legacycontract.api.LegacyUserAccount;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of the minimal legacy identity read contract.
 *
 * <p>Queries are parameterized, route through the current tenant's read-only CORE datasource, and
 * intentionally omit password columns.</p>
 */
@Repository
public class JdbcLegacyIdentityQuery implements LegacyIdentityQuery {
    private final JdbcClient core;

    /**
     * Creates the adapter with the tenant-aware CORE client.
     *
     * @param core JDBC client routed by trusted {@code TenantContext}
     */
    public JdbcLegacyIdentityQuery(@Qualifier("coreJdbcClient") JdbcClient core) { this.core = core; }

    /**
     * Reads one enabled account from {@code public.tbmuser} and normalizes its assigned roles.
     *
     * @param userId exact user identifier bound as a SQL parameter
     * @return active account projection, or empty when absent or disabled
     */
    @Override public Optional<LegacyUserAccount> findActiveUser(String userId) {
        return core.sql("""
                select userid, coalesce(nullif(btrim(usernama), ''), userid) display_name,
                       coalesce(aktif, true) active, userrole, user_role2, user_role3, user_role4, user_role5
                  from public.tbmuser
                 where userid = :userId and coalesce(aktif, true)
                """).param("userId", userId)
                .query((rs, row) -> {
                    List<String> roles = Stream.of(rs.getString("userrole"), rs.getString("user_role2"),
                                    rs.getString("user_role3"), rs.getString("user_role4"), rs.getString("user_role5"))
                            .filter(value -> value != null && !value.isBlank()).distinct().toList();
                    return new LegacyUserAccount(rs.getString("userid"), rs.getString("display_name"),
                            rs.getBoolean("active"), rs.getString("userrole"), roles);
                }).optional();
    }
}
