package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.DatabaseRole;
import id.aisnext.tenant.api.ResolvedTenant;
import id.aisnext.tenant.api.TenantCatalog;
import id.aisnext.tenant.api.TenantDataSourceKey;
import id.aisnext.tenant.api.TenantDatabaseDescriptor;
import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantMode;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class JdbcTenantCatalog implements TenantCatalog {
    private final JdbcClient control;

    public JdbcTenantCatalog(JdbcClient control) {
        this.control = control;
    }

    @Override public Optional<ResolvedTenant> findByTrustedHost(String normalizedHost) {
        return control.sql("""
                select t.tenant_key, t.name, t.mode, t.default_locale, t.timezone
                  from tenant_domain d
                  join tenant t on t.id = d.tenant_id
                 where d.normalized_domain = :host
                   and d.status = 'ACTIVE'
                   and t.status in ('READY', 'ACTIVE')
                """)
                .param("host", normalizedHost)
                .query((rs, row) -> new ResolvedTenant(
                        new TenantId(rs.getString("tenant_key")),
                        rs.getString("name"),
                        TenantMode.valueOf(rs.getString("mode")),
                        parseLocale(rs.getString("default_locale")),
                        ZoneId.of(rs.getString("timezone"))))
                .optional();
    }

    @Override public Optional<TenantDatabaseDescriptor> findDatabase(TenantDataSourceKey key) {
        return control.sql("""
                select jdbc_url, credential_reference, maximum_pool_size, read_only
                  from tenant_database
                 where tenant_id = (select id from tenant where tenant_key = :tenant)
                   and database_role = :role
                   and enabled = true
                """)
                .param("tenant", key.tenantId().value())
                .param("role", key.databaseRole().name())
                .query((rs, row) -> new TenantDatabaseDescriptor(
                        key,
                        rs.getString("jdbc_url"),
                        rs.getString("credential_reference"),
                        rs.getInt("maximum_pool_size"),
                        rs.getBoolean("read_only")))
                .optional();
    }

    private static Locale parseLocale(String value) {
        String[] parts = value.replace('-', '_').split("_", 3);
        return parts.length == 1 ? Locale.of(parts[0]) : Locale.of(parts[0], parts[1]);
    }
}
