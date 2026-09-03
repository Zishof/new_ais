package id.aisnext.attendance.web;

import java.net.URI;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Produces a stable, non-sensitive API response while the tenant CORE database is unavailable. */
@RestControllerAdvice(assignableTypes = DailyAttendanceApiController.class)
public class DailyAttendanceApiExceptionHandler {
    /** Creates the stateless attendance API error mapper. */
    public DailyAttendanceApiExceptionHandler() {
    }

    /**
     * Converts connection, query, and transaction failures to a retryable HTTP 503 problem.
     *
     * @param exception internal database failure; its detail is deliberately not returned
     * @return safe service-unavailable problem detail
     */
    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ProblemDetail unavailable(RuntimeException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Data kehadiran sementara tidak tersedia; silakan coba lagi");
        detail.setType(URI.create("urn:ais-next:problem:attendance-unavailable"));
        return detail;
    }
}
