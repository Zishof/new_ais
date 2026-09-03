package id.aisnext.attendance.web;

import id.aisnext.attendance.api.AttendanceRecordState;
import id.aisnext.attendance.application.DailyAttendanceService;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Accessible server-rendered page for daily employee-attendance monitoring. */
@Controller
public class DailyAttendancePageController {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Jakarta");

    private final DailyAttendanceService service;

    /**
     * Creates the page controller.
     *
     * @param service validated daily attendance application boundary
     */
    public DailyAttendancePageController(DailyAttendanceService service) {
        this.service = service;
    }

    /**
     * Renders a selected day, defaulting only the browser view to today's Jakarta date.
     *
     * @param date optional ISO local date from the browser filter
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param q employee name or number fragment
     * @param recordState {@code ALL}, {@code RECORDED}, or {@code UNRECORDED}
     * @param model model receiving the result and preserved filter state
     * @return daily attendance template name
     */
    @GetMapping("/attendance/daily")
    public String list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "ALL") String recordState,
            Model model) {
        LocalDate selectedDate = date == null ? LocalDate.now(BUSINESS_ZONE) : date;
        AttendanceRecordState selectedState = AttendanceRecordState.parse(recordState);
        model.addAttribute("result", service.findDaily(selectedDate, page, size, q, selectedState));
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("query", q);
        model.addAttribute("recordState", selectedState.name());
        return "attendance/daily";
    }
}
