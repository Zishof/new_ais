package id.aisnext.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import id.aisnext.security.api.NonceStore;
import id.aisnext.security.infrastructure.JdbcNonceStore;
import id.aisnext.tenant.api.TenantCatalog;
import id.aisnext.tenant.infrastructure.JdbcTenantCatalog;
import org.flywaydb.core.Flyway;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "ais.control.enabled", havingValue = "true", matchIfMissing = true)
public class ControlPlaneConfiguration {
    @Bean(destroyMethod = "close") HikariDataSource controlDataSource(ControlPlaneProperties properties) {
        if (properties.getUsername() == null || properties.getPassword() == null) {
            throw new IllegalStateException("AIS_CONTROL_DB_USERNAME and AIS_CONTROL_DB_PASSWORD are required");
        }
        HikariConfig config = new HikariConfig();
        config.setPoolName("ais-control");
        config.setJdbcUrl(properties.getJdbcUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setMaximumPoolSize(properties.getMaximumPoolSize());
        config.setMinimumIdle(0);
        // Control-plane writes are explicit application state (migrations, nonce use,
        // tenant metadata). JdbcClient is intentionally used without an ambient
        // transaction here, so the small control pool must commit each statement.
        config.setAutoCommit(true);
        return new HikariDataSource(config);
    }

    @Bean(initMethod = "migrate") Flyway controlPlaneFlyway(HikariDataSource controlDataSource) {
        return Flyway.configure().dataSource(controlDataSource).locations("classpath:db/control").load();
    }

    @Bean JdbcClient controlJdbcClient(HikariDataSource controlDataSource, Flyway controlPlaneFlyway) {
        return JdbcClient.create(controlDataSource);
    }

    @Bean TenantCatalog tenantCatalog(JdbcClient controlJdbcClient) {
        return new JdbcTenantCatalog(controlJdbcClient);
    }

    @Bean NonceStore nonceStore(JdbcClient controlJdbcClient) {
        return new JdbcNonceStore(controlJdbcClient);
    }

    @Bean HealthIndicator controlPlaneHealthIndicator(JdbcClient controlJdbcClient) {
        return () -> {
            try {
                controlJdbcClient.sql("select 1").query(Integer.class).single();
                return Health.up().withDetail("database", "ais_next_control").build();
            } catch (RuntimeException exception) {
                return Health.down(exception).build();
            }
        };
    }
}
