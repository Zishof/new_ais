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

@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public final class RequestIdFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Request-Id";

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
