package id.aisnext.academic.web;

import java.net.URI;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Produces a stable, non-sensitive API response when the student source is unavailable. */
@RestControllerAdvice(assignableTypes = StudentDirectoryApiController.class)
public class StudentDirectoryApiExceptionHandler {
    /** Creates the stateless student-directory API error mapper. */
    public StudentDirectoryApiExceptionHandler() {
    }

    /**
     * Converts connection, query, and transaction failures to a retryable HTTP 503 problem.
     *
     * @param exception internal database failure whose detail is deliberately hidden
     * @return safe service-unavailable problem detail
     */
    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ProblemDetail unavailable(RuntimeException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Direktori siswa sementara tidak tersedia; silakan coba lagi");
        detail.setType(URI.create("urn:ais-next:problem:student-directory-unavailable"));
        return detail;
    }
}
