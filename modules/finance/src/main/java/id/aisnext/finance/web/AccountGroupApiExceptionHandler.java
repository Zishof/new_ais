package id.aisnext.finance.web;

import java.net.URI;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Produces a stable, non-sensitive API response when the finance source is unavailable. */
@RestControllerAdvice(assignableTypes = AccountGroupApiController.class)
public class AccountGroupApiExceptionHandler {
    /** Creates the stateless account-group API error mapper. */
    public AccountGroupApiExceptionHandler() {
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
                "Grup akun keuangan sementara tidak tersedia; silakan coba lagi");
        detail.setType(URI.create("urn:ais-next:problem:account-groups-unavailable"));
        return detail;
    }
}
