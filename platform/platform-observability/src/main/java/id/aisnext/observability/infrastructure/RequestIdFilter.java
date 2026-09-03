package id.aisnext.observability.infrastructure;

import id.aisnext.kernel.api.RequestId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates or generates a request correlation ID and echoes it in every successful downstream
 * response.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public final class RequestIdFilter extends OncePerRequestFilter {
    /** HTTP header used to accept and return the request correlation identifier. */
    public static final String HEADER = "X-Request-Id";

    /**
     * Creates the stateless request-correlation filter.
     */
    public RequestIdFilter() {
    }

    /**
     * Establishes the request ID before tenant resolution and controller invocation.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param chain remaining servlet filter chain
     * @throws ServletException when downstream servlet processing fails
     * @throws IOException when response or downstream I/O fails
     */
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                              FilterChain chain) throws ServletException, IOException {
        String supplied = request.getHeader(HEADER);
        RequestId requestId;
        try {
            requestId = supplied == null ? RequestId.generate() : new RequestId(supplied);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request id");
            return;
        }
        request.setAttribute("requestId", requestId);
        response.setHeader(HEADER, requestId.value());
        chain.doFilter(request, response);
    }
}
