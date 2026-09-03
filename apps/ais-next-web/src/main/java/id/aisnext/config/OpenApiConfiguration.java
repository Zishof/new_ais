package id.aisnext.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures the machine-readable contract for versioned AIS Next HTTP APIs. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {
    /**
     * Creates the Spring configuration definition for OpenAPI metadata.
     */
    public OpenApiConfiguration() {
    }

    /**
     * Creates the root OpenAPI description without changing any legacy API contract.
     *
     * @return API metadata published by springdoc
     */
    @Bean OpenAPI aisNextOpenApi() {
        return new OpenAPI().info(new Info().title("AIS Next API").version("v1")
                .description("Versioned read-side API. Legacy contracts remain unchanged."));
    }
}
