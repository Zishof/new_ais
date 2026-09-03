package id.aisnext.attendance.web;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/** Renders a safe retry page while the daily attendance data source is unavailable. */
@ControllerAdvice(assignableTypes = DailyAttendancePageController.class)
public class DailyAttendancePageExceptionHandler {
    /** Creates the stateless attendance page error mapper. */
    public DailyAttendancePageExceptionHandler() {
    }

    /**
     * Converts connection, query, and transaction failures to an HTTP 503 retry page.
     *
     * @param exception internal database failure; its detail is deliberately not rendered
     * @return unavailable view with an explicit service-unavailable status
     */
    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ModelAndView unavailable(RuntimeException exception) {
        ModelAndView view = new ModelAndView("attendance/unavailable");
        view.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
        return view;
    }
}
