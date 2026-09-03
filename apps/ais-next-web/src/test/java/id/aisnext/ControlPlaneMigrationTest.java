package id.aisnext;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class ControlPlaneMigrationTest {
    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("ais_next_control")
            .withUsername("ais_next")
            .withPassword("test-only");

    @Test
    void controlPlaneMigrationCreatesOnlyItsOwnedTables() throws Exception {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/control")
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.prepareStatement("""
                     select count(*)
                     from information_schema.tables
                     where table_schema = 'public'
                       and table_name in (
                         'tenant', 'tenant_domain', 'tenant_database', 'tenant_module_route',
                         'tenant_feature_flag', 'tenant_api_client', 'tenant_schema_fingerprint',
                         'tenant_migration_state', 'security_handoff_nonce', 'audit_event',
                         'outbox_event', 'file_saga', 'tenant_secret_reference'
                       )
                     """)) {
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt(1)).isEqualTo(13);
            }
        }
    }
}
