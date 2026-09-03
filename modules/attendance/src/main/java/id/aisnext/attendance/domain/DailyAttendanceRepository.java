package id.aisnext.attendance.domain;

import id.aisnext.attendance.api.AttendanceRecordState;
import id.aisnext.attendance.api.DailyAttendance;
import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import java.time.LocalDate;

/** Read-only persistence port for the audited daily employee-attendance projection. */
@FunctionalInterface
public interface DailyAttendanceRepository {
    /**
     * Reads a deterministic employee page without invoking legacy repair behavior.
     *
     * @param date selected local attendance date
     * @param query validated page and employee filter
     * @param state requested recorded-row state
     * @return matching immutable projection page
     */
    PageResult<DailyAttendance> findDaily(LocalDate date, PageQuery query, AttendanceRecordState state);
}
