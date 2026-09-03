package id.aisnext.legacyrbac.infrastructure;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.legacycontract.api.LegacyMenuPrivilege;
import id.aisnext.legacycontract.api.LegacyRoleDetail;
import id.aisnext.legacycontract.api.LegacyRoleQuery;
import id.aisnext.legacycontract.api.LegacyRoleSummary;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcLegacyRoleQuery implements LegacyRoleQuery {
    private final JdbcClient core;

    public JdbcLegacyRoleQuery(@Qualifier("coreJdbcClient") JdbcClient core) { this.core = core; }

    @Override public PageResult<LegacyRoleSummary> findRoles(PageQuery query) {
        String pattern = "%" + query.filter().toLowerCase(java.util.Locale.ROOT) + "%";
        long total = core.sql("""
                select count(*) from public.tbmrole
                 where lower(roleid) like :pattern
                    or lower(coalesce(rolename, '')) like :pattern
                """).param("pattern", pattern).query(Long.class).single();
        List<LegacyRoleSummary> roles = core.sql("""
                select roleid, coalesce(nullif(btrim(rolename), ''), roleid) as display_name,
                       coalesce(aktif, true) as active
                  from public.tbmrole
                 where lower(roleid) like :pattern
                    or lower(coalesce(rolename, '')) like :pattern
                 order by lower(coalesce(nullif(btrim(rolename), ''), roleid)), roleid
                 limit :limit offset :offset
                """)
                .param("pattern", pattern).param("limit", query.size()).param("offset", query.offset())
                .query((rs, row) -> new LegacyRoleSummary(rs.getString("roleid"),
                        rs.getString("display_name"), rs.getBoolean("active"))).list();
        return new PageResult<>(roles, query.page(), query.size(), total);
    }

    @Override public Optional<LegacyRoleDetail> findRole(String roleId) {
        Optional<LegacyRoleSummary> role = core.sql("""
                select roleid, coalesce(nullif(btrim(rolename), ''), roleid) as display_name,
                       coalesce(aktif, true) as active
                  from public.tbmrole where roleid = :roleId
                """).param("roleId", roleId)
                .query((rs, row) -> new LegacyRoleSummary(rs.getString("roleid"),
                        rs.getString("display_name"), rs.getBoolean("active"))).optional();
        return role.map(summary -> new LegacyRoleDetail(summary.id(), summary.name(), summary.active(), menus(roleId)));
    }

    private List<LegacyMenuPrivilege> menus(String roleId) {
        return core.sql("""
                select m.id, coalesce(m.label, '') label, coalesce(m.url, '') url,
                       coalesce(m.root, 0) parent_code, coalesce(m.child, 0) node_code,
                       coalesce(p._read, 0) can_read, coalesce(p._create, 0) can_create,
                       coalesce(p._update, 0) can_update, coalesce(p._delete, 0) can_delete,
                       coalesce(p._approve, 0) can_approve, coalesce(p._reject, 0) can_reject
                  from public.job_has_menu j
                  join public.menu m on m.id = j.menu
                  left join public.role_privilage p on p.role = j.job and p.menu = j.menu
                 where j.job = :roleId and coalesce(m.aktif, true)
                 order by coalesce(m.nomorurut, 0), coalesce(m.root, 0), coalesce(m.child, 0), m.id
                """).param("roleId", roleId)
                .query((rs, row) -> new LegacyMenuPrivilege(rs.getLong("id"), rs.getString("label"),
                        rs.getString("url"), rs.getLong("parent_code"), rs.getLong("node_code"),
                        rs.getInt("can_read") == 1, rs.getInt("can_create") == 1,
                        rs.getInt("can_update") == 1, rs.getInt("can_delete") == 1,
                        rs.getInt("can_approve") == 1, rs.getInt("can_reject") == 1)).list();
    }
}
