package id.aisnext.api.web;

import id.aisnext.security.api.InvalidHandoffTokenException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converts expected validation and handoff failures into stable RFC 9457 problem responses. */
@RestControllerAdvice
public final class ApiExceptionHandler {
    /**
     * Creates the stateless exception-to-problem response mapper.
     */
    public ApiExceptionHandler() {
    }

    /**
     * Maps invalid caller input to HTTP 400.
     *
     * @param exception validation failure containing a safe client-facing message
     * @return problem detail with the AIS Next bad-request type URI
     */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        detail.setType(URI.create("urn:ais-next:problem:bad-request"));
        return detail;
    }

    /**
     * Maps expired, malformed, tampered, or replayed handoff tokens to HTTP 401.
     *
     * @param exception deliberately non-specific token verification failure
     * @return problem detail that does not disclose which token check failed
     */
    @ExceptionHandler(InvalidHandoffTokenException.class)
    ProblemDetail unauthorized(InvalidHandoffTokenException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        detail.setType(URI.create("urn:ais-next:problem:invalid-handoff"));
        return detail;
    }
}
