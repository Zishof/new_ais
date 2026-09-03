package id.aisnext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.modulith.Modulithic;

/**
 * Boots the AIS Next modular monolith as an executable Spring Boot application.
 *
 * <p>The default JDBC, Flyway, and generated-user auto-configurations are disabled because AIS
 * Next owns a dedicated control-plane datasource and resolves tenant datasources lazily. Security
 * identities enter through the explicit legacy handoff bridge instead of a generated password.</p>
 */
@Modulithic
@ConfigurationPropertiesScan
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, FlywayAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class})
public class AisNextApplication {
    /**
     * Creates the Spring Boot application definition used by framework bootstrap and tests.
     */
    public AisNextApplication() {
    }

    /**
     * Starts AIS Next with configuration supplied through command-line arguments and the process
     * environment.
     *
     * @param args Spring Boot command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AisNextApplication.class, args);
    }
}
