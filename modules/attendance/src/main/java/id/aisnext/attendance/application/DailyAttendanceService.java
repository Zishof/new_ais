package id.aisnext.attendance.application;

import id.aisnext.attendance.api.AttendanceRecordState;
import id.aisnext.attendance.api.DailyAttendance;
import id.aisnext.attendance.domain.DailyAttendanceRepository;
import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application boundary for the read-only daily employee-attendance monitor. */
@Service
public class DailyAttendanceService {
    /** Maximum rows accepted from one browser or API request. */
    public static final int MAXIMUM_PAGE_SIZE = 100;

    private final DailyAttendanceRepository repository;

    /**
     * Creates the service from the read-only projection port.
     *
     * @param repository tenant-aware daily attendance projection
     */
    public DailyAttendanceService(DailyAttendanceRepository repository) {
        this.repository = repository;
    }

    /**
     * Validates request bounds and returns one daily attendance page in a read-only transaction.
     *
     * @param date selected local date; never inferred by the API
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param filter case-insensitive employee name or number fragment
     * @param recordState closed recorded-row filter
     * @return deterministic employee attendance page
     * @throws NullPointerException when date or record state is absent
     * @throws IllegalArgumentException when page or size is outside its accepted bounds
     */
    @Transactional(transactionManager = "coreTransactionManager", readOnly = true)
    public PageResult<DailyAttendance> findDaily(LocalDate date, int page, int size, String filter,
                                                  AttendanceRecordState recordState) {
        Objects.requireNonNull(date, "date is required");
        Objects.requireNonNull(recordState, "recordState is required");
        if (size > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        return repository.findDaily(date, new PageQuery(page, size, filter), recordState);
    }
}
