package id.aisnext.attendance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import id.aisnext.attendance.api.AttendanceRecordState;
import id.aisnext.attendance.domain.DailyAttendanceRepository;
import id.aisnext.kernel.api.PageQuery;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Verifies request validation at the read-only daily attendance application boundary. */
class DailyAttendanceServiceTest {
    private final DailyAttendanceRepository repository = mock(DailyAttendanceRepository.class);
    private final DailyAttendanceService service = new DailyAttendanceService(repository);

    /** Creates the service test fixture. */
    DailyAttendanceServiceTest() {
    }

    /** Confirms parsing is case-insensitive and blank input selects the safe all-records state. */
    @Test
    void parsesClosedRecordStateVocabulary() {
        assertThat(AttendanceRecordState.parse(null)).isEqualTo(AttendanceRecordState.ALL);
        assertThat(AttendanceRecordState.parse(" recorded ")).isEqualTo(AttendanceRecordState.RECORDED);
        assertThat(AttendanceRecordState.parse("unrecorded")).isEqualTo(AttendanceRecordState.UNRECORDED);
    }

    /** Confirms unsupported record-state values fail before repository access. */
    @Test
    void rejectsUnknownRecordState() {
        assertThatThrownBy(() -> AttendanceRecordState.parse("RECORDED' or true --"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("recordState must be ALL, RECORDED, or UNRECORDED");
    }

    /** Confirms validated date, paging, trimmed filter, and state reach the read port unchanged. */
    @Test
    void delegatesValidatedProjectionRequest() {
        LocalDate date = LocalDate.of(2026, 9, 4);

        service.findDaily(date, 2, 25, "  Budi  ", AttendanceRecordState.RECORDED);

        ArgumentCaptor<PageQuery> query = ArgumentCaptor.forClass(PageQuery.class);
        verify(repository).findDaily(org.mockito.ArgumentMatchers.eq(date), query.capture(),
                org.mockito.ArgumentMatchers.eq(AttendanceRecordState.RECORDED));
        assertThat(query.getValue()).isEqualTo(new PageQuery(2, 25, "Budi"));
    }

    /** Confirms a request cannot exceed the audited 100-row response bound. */
    @Test
    void rejectsOversizedPage() {
        assertThatThrownBy(() -> service.findDaily(LocalDate.of(2026, 9, 4), 0, 101, "",
                AttendanceRecordState.ALL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("size must be between 1 and 100");
    }

    /** Confirms absence of a selected date fails before repository access. */
    @Test
    void rejectsMissingDate() {
        assertThatThrownBy(() -> service.findDaily(null, 0, 25, "", AttendanceRecordState.ALL))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("date is required");
    }
}
