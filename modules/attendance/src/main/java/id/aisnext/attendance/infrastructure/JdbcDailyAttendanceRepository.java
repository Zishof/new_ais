package id.aisnext.attendance.infrastructure;

import id.aisnext.attendance.api.AttendanceRecordState;
import id.aisnext.attendance.api.DailyAttendance;
import id.aisnext.attendance.domain.DailyAttendanceRepository;
import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Parameterized JDBC adapter for daily attendance over the immutable legacy CORE schema.
 *
 * <p>The adapter executes SELECT statements only. A lateral query selects the greatest attendance
 * ID for duplicate employee/date rows without materializing mutable legacy entities.</p>
 */
@Repository
public class JdbcDailyAttendanceRepository implements DailyAttendanceRepository {
    private static final String EMPLOYEE_PREDICATE = """
            (p.aktif is null or p.aktif)
            and (lower(coalesce(p.nama, '')) like :pattern
                 or lower(coalesce(p.mycode, '')) like :pattern
                 or lower(coalesce(p.code, '')) like :pattern)
            and (:recordState = 'ALL'
                 or (:recordState = 'RECORDED' and exists (
                       select 1 from public.status_kehadiran_karyawan_harian present
                        where present.pegawai = p.id and present.tanggal = :selectedDate))
                 or (:recordState = 'UNRECORDED' and not exists (
                       select 1 from public.status_kehadiran_karyawan_harian missing
                        where missing.pegawai = p.id and missing.tanggal = :selectedDate)))
            """;

    private static final String SELECT_DAILY = """
            select p.id employee_id,
                   coalesce(nullif(btrim(p.mycode), ''), nullif(btrim(p.code), ''), '') employee_number,
                   coalesce(p.nama, '') employee_name,
                   coalesce(p.aktif, true) employee_active,
                   cast(:selectedDate as date) selected_date,
                   a.id attendance_id,
                   coalesce(s.kode, '') status_code,
                   coalesce(s.nama, '') status_name,
                   a.masuk_jam check_in,
                   a.pulang_jam check_out,
                   a.keterangan_absen note
              from public.pegawai p
              left join lateral (
                    select daily.id, daily.statusabsensi, daily.masuk_jam,
                           daily.pulang_jam, daily.keterangan_absen
                      from public.status_kehadiran_karyawan_harian daily
                     where daily.pegawai = p.id and daily.tanggal = :selectedDate
                     order by daily.id desc
                     limit 1
              ) a on true
              left join public.statusabsensi s on s.id = a.statusabsensi
             where
            """;

    private final JdbcClient core;

    /**
     * Creates the adapter with the tenant-routed CORE JDBC client.
     *
     * @param core tenant-aware projection client
     */
    public JdbcDailyAttendanceRepository(@Qualifier("coreJdbcClient") JdbcClient core) {
        this.core = core;
    }

    /**
     * Reads and counts one employee page using the exact audited predicates.
     *
     * @param date selected local attendance date
     * @param query validated page and employee filter
     * @param state requested recorded-row state
     * @return matching immutable projection page
     */
    @Override
    public PageResult<DailyAttendance> findDaily(LocalDate date, PageQuery query,
                                                  AttendanceRecordState state) {
        String pattern = "%" + query.filter().toLowerCase(java.util.Locale.ROOT) + "%";
        long total = bind(core.sql("select count(*) from public.pegawai p where " + EMPLOYEE_PREDICATE),
                date, pattern, state).query(Long.class).single();
        List<DailyAttendance> rows = bind(core.sql(SELECT_DAILY + EMPLOYEE_PREDICATE
                        + " order by lower(coalesce(p.nama, '')), p.id limit :limit offset :offset"),
                date, pattern, state)
                .param("limit", query.size())
                .param("offset", query.offset())
                .query(this::map)
                .list();
        return new PageResult<>(rows, query.page(), query.size(), total);
    }

    /**
     * Binds the shared date, text, and enum parameters to one SELECT specification.
     *
     * @param statement incomplete JDBC statement
     * @param date selected local attendance date
     * @param pattern normalized SQL LIKE pattern
     * @param state closed record-state value
     * @return statement with all shared values bound
     */
    private static JdbcClient.StatementSpec bind(JdbcClient.StatementSpec statement, LocalDate date,
                                                   String pattern, AttendanceRecordState state) {
        return statement.param("selectedDate", date)
                .param("pattern", pattern)
                .param("recordState", state.name());
    }

    /**
     * Maps one JDBC row without leaking a mutable persistence entity.
     *
     * @param resultSet current result row
     * @param rowNumber zero-based row number supplied by Spring JDBC
     * @return immutable daily attendance projection
     * @throws SQLException when the JDBC driver cannot read a selected value
     */
    private DailyAttendance map(ResultSet resultSet, int rowNumber) throws SQLException {
        Long attendanceId = resultSet.getObject("attendance_id", Long.class);
        return new DailyAttendance(
                resultSet.getLong("employee_id"),
                resultSet.getString("employee_number"),
                resultSet.getString("employee_name"),
                resultSet.getBoolean("employee_active"),
                resultSet.getObject("selected_date", LocalDate.class),
                attendanceId,
                resultSet.getString("status_code"),
                resultSet.getString("status_name"),
                resultSet.getObject("check_in", LocalTime.class),
                resultSet.getObject("check_out", LocalTime.class),
                resultSet.getString("note"),
                attendanceId == null ? AttendanceRecordState.UNRECORDED : AttendanceRecordState.RECORDED);
    }
}
