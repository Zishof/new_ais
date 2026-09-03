package id.aisnext.attendance.web;

import id.aisnext.attendance.api.AttendanceRecordState;
import id.aisnext.attendance.api.DailyAttendance;
import id.aisnext.attendance.application.DailyAttendanceService;
import id.aisnext.kernel.api.PageResult;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Versioned JSON boundary for the read-only daily employee-attendance monitor. */
@RestController
@RequestMapping("/api/v1/attendance/daily")
public class DailyAttendanceApiController {
    private final DailyAttendanceService service;

    /**
     * Creates the API controller.
     *
     * @param service validated daily attendance application boundary
     */
    public DailyAttendanceApiController(DailyAttendanceService service) {
        this.service = service;
    }

    /**
     * Returns a filtered and server-paged daily employee projection.
     *
     * @param date required ISO local date
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param q employee name or number fragment
     * @param recordState {@code ALL}, {@code RECORDED}, or {@code UNRECORDED}
     * @return immutable attendance result page
     */
    @GetMapping
    public PageResult<DailyAttendance> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "ALL") String recordState) {
        return service.findDaily(date, page, size, q, AttendanceRecordState.parse(recordState));
    }
}
