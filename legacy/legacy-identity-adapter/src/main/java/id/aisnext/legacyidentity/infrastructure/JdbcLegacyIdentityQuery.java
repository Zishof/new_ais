package id.aisnext.legacyidentity.infrastructure;

import id.aisnext.legacycontract.api.LegacyIdentityQuery;
import id.aisnext.legacycontract.api.LegacyUserAccount;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcLegacyIdentityQuery implements LegacyIdentityQuery {
    private final JdbcClient core;

    public JdbcLegacyIdentityQuery(@Qualifier("coreJdbcClient") JdbcClient core) { this.core = core; }

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
