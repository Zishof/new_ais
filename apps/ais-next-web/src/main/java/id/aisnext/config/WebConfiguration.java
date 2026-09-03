package id.aisnext.config;

import id.aisnext.observability.infrastructure.RequestIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WebConfiguration {
    @Bean RequestIdFilter requestIdFilter() { return new RequestIdFilter(); }
}
