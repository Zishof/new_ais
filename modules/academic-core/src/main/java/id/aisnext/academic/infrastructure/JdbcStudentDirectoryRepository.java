package id.aisnext.academic.infrastructure;

import id.aisnext.academic.api.StudentDirectoryEntry;
import id.aisnext.academic.domain.StudentDirectoryRepository;
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
 * SELECT-only JDBC adapter for the data-minimized legacy school-student projection.
 *
 * <p>The active role is joined inside each query so browser parameters cannot widen its foundation
 * or school scope. The query enumerates approved columns and never hydrates the broad legacy
 * student entity.</p>
 */
@Repository
public class JdbcStudentDirectoryRepository implements StudentDirectoryRepository {
    private static final String FROM_AND_PREDICATE = """
              from sekolah.siswa s
              join public.tbmrole role_scope
                on role_scope.roleid = :activeRoleId and role_scope.aktif
              left join sekolah.sekolah school on school.id = s.sekolah_id
              left join sekolah.kelas current_class on current_class.id = s.current_kelas_id
              left join sekolah.status_awal_siswa initial_status on initial_status.id = s.status_awal_siswa
              left join sekolah.status_keluar_siswa exit_status on exit_status.id = s.status_keluar_siswa
             where s.nama_siswa is not null
               and btrim(s.nama_siswa) <> ''
               and s.sekolah_id is not null
               and (s.aktif is null or s.aktif)
               and (role_scope.yayasan is null or s.yayasan_id = role_scope.yayasan)
               and (role_scope.sekolah is null or s.sekolah_id = role_scope.sekolah)
               and (lower(coalesce(s.nomor_induk, '')) like :pattern escape '\\'
                    or lower(s.nama_siswa) like :pattern escape '\\')
            """;

    private static final String SELECT_APPROVED_FIELDS = """
            select s.id student_id,
                   coalesce(btrim(s.nomor_induk), '') student_number,
                   btrim(s.nama_siswa) student_name,
                   s.tahun_masuk entry_year,
                   coalesce(school.nama, '') school_name,
                   coalesce(current_class.nama, '') current_class_name,
                   coalesce(initial_status.nama, '') initial_status_name,
                   coalesce(exit_status.nama, '') exit_status_name,
                   coalesce(s.aktif, true) active
            """;

    private final JdbcClient core;

    /**
     * Creates the adapter with the tenant-routed CORE JDBC client.
     *
     * @param core tenant-aware projection client
     */
    public JdbcStudentDirectoryRepository(@Qualifier("coreJdbcClient") JdbcClient core) {
        this.core = core;
    }

    /**
     * Counts and reads one deterministic page using the exact audited security predicates.
     *
     * @param activeRoleId exact active legacy role selected during handoff
     * @param query validated paging and literal text filter
     * @return role-scoped, active-student page
     */
    @Override
    public PageResult<StudentDirectoryEntry> findVisibleActiveStudents(String activeRoleId,
                                                                        PageQuery query) {
        String pattern = toLikePattern(query.filter());
        long total = bind(core.sql("select count(*)" + FROM_AND_PREDICATE), activeRoleId, pattern)
                .query(Long.class)
                .single();
        List<StudentDirectoryEntry> students = bind(core.sql(SELECT_APPROVED_FIELDS
                        + FROM_AND_PREDICATE
                        + " order by s.tahun_masuk desc, s.nomor_induk asc, s.id asc"
                        + " limit :limit offset :offset"), activeRoleId, pattern)
                .param("limit", query.size())
                .param("offset", query.offset())
                .query(this::map)
                .list();
        return new PageResult<>(students, query.page(), query.size(), total);
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
     * Binds the active role and escaped filter shared by count and data statements.
     *
     * @param statement incomplete SELECT statement
     * @param activeRoleId exact active legacy role identifier
     * @param pattern escaped lower-case LIKE pattern
     * @return statement with every shared value bound
     */
    private static JdbcClient.StatementSpec bind(JdbcClient.StatementSpec statement,
                                                   String activeRoleId, String pattern) {
        return statement.param("activeRoleId", activeRoleId).param("pattern", pattern);
    }

    /**
     * Maps one JDBC row into the approved immutable response shape.
     *
     * @param resultSet current result row
     * @param rowNumber zero-based row number supplied by Spring JDBC
     * @return minimized student directory entry
     * @throws SQLException when the JDBC driver cannot read an approved value
     */
    private StudentDirectoryEntry map(ResultSet resultSet, int rowNumber) throws SQLException {
        return new StudentDirectoryEntry(
                resultSet.getLong("student_id"),
                resultSet.getString("student_number"),
                resultSet.getString("student_name"),
                resultSet.getInt("entry_year"),
                resultSet.getString("school_name"),
                resultSet.getString("current_class_name"),
                resultSet.getString("initial_status_name"),
                resultSet.getString("exit_status_name"),
                resultSet.getBoolean("active"));
    }
}
