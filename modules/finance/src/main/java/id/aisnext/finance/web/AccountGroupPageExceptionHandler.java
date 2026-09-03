package id.aisnext.finance.web;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/** Renders a safe retry page while the account-group source is unavailable. */
@ControllerAdvice(assignableTypes = AccountGroupPageController.class)
public class AccountGroupPageExceptionHandler {
    /** Creates the stateless account-group page error mapper. */
    public AccountGroupPageExceptionHandler() {
    }

    /**
     * Converts connection, query, and transaction failures to an HTTP 503 retry page.
     *
     * @param exception internal database failure whose detail is deliberately hidden
     * @return unavailable view with an explicit service-unavailable status
     */
    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ModelAndView unavailable(RuntimeException exception) {
        ModelAndView view = new ModelAndView("finance/unavailable");
        view.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
        return view;
    }
}
