package id.aisnext.config;

import id.aisnext.observability.infrastructure.RequestIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provides cross-cutting servlet filters that are independent of tenant routing. */
@Configuration(proxyBeanMethods = false)
public class WebConfiguration {
    /**
     * Creates the Spring configuration definition for cross-cutting web filters.
     */
    public WebConfiguration() {
    }

    /**
     * Creates the filter that validates or assigns a request correlation identifier.
     *
     * @return request-ID propagation filter
     */
    @Bean RequestIdFilter requestIdFilter() { return new RequestIdFilter(); }
}
