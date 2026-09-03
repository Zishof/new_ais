package id.aisnext.organization.web;

import id.aisnext.organization.domain.SchoolTypeConflictException;
import id.aisnext.organization.domain.SchoolTypeNotFoundException;
import id.aisnext.organization.domain.SchoolTypeValidationException;
import id.aisnext.tenant.api.WriteOwnershipDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps expected school-type failures to stable, non-sensitive HTTP responses. */
@RestControllerAdvice(assignableTypes = {SchoolTypeApiController.class, SchoolTypeWorkbookController.class})
public class SchoolTypeApiExceptionHandler {
    /** Creates the stateless API exception mapper. */
    public SchoolTypeApiExceptionHandler() {
    }

    /**
     * Maps invalid fields, sort values, and missing concurrency headers to HTTP 400.
     *
     * @param exception expected invalid-request failure
     * @return stable bad-request response
     */
    @ExceptionHandler({SchoolTypeValidationException.class, IllegalArgumentException.class,
            MissingRequestHeaderException.class})
    public ResponseEntity<SchoolTypeApiError> badRequest(Exception exception) {
        return ResponseEntity.badRequest().body(new SchoolTypeApiError("INVALID_REQUEST", exception.getMessage()));
    }

    /**
     * Maps a missing legacy identifier to HTTP 404.
     *
     * @param exception expected missing-row failure
     * @return stable not-found response
     */
    @ExceptionHandler(SchoolTypeNotFoundException.class)
    public ResponseEntity<SchoolTypeApiError> notFound(SchoolTypeNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SchoolTypeApiError("NOT_FOUND", exception.getMessage()));
    }

    /**
     * Maps duplicate, stale, and referenced-delete failures to HTTP 409.
     *
     * @param exception expected data conflict
     * @return stable conflict response
     */
    @ExceptionHandler(SchoolTypeConflictException.class)
    public ResponseEntity<SchoolTypeApiError> conflict(SchoolTypeConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new SchoolTypeApiError("CONFLICT", exception.getMessage()));
    }

    /**
     * Maps a control-plane ownership mismatch to HTTP 503 so clients never retry as validation.
     *
     * @param exception fail-closed ownership denial
     * @return service-unavailable response without exposing control SQL
     */
    @ExceptionHandler(WriteOwnershipDeniedException.class)
    public ResponseEntity<SchoolTypeApiError> ownership(WriteOwnershipDeniedException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new SchoolTypeApiError("WRITE_OWNER_UNAVAILABLE", exception.getMessage()));
    }
}
