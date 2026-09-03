package id.aisnext.api.web;

import id.aisnext.security.api.InvalidHandoffTokenException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        detail.setType(URI.create("urn:ais-next:problem:bad-request"));
        return detail;
    }

    @ExceptionHandler(InvalidHandoffTokenException.class)
    ProblemDetail unauthorized(InvalidHandoffTokenException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        detail.setType(URI.create("urn:ais-next:problem:invalid-handoff"));
        return detail;
    }
}
