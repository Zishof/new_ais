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

/**
 * Read-only tenant catalog backed by the AIS Next control-plane database.
 *
 * <p>Queries are intentionally limited to active host mappings and enabled database descriptors.
 * Credential values are never stored in the catalog; only their external references are read.</p>
 */
public final class JdbcTenantCatalog implements TenantCatalog {
    private final JdbcClient control;

    /**
     * Creates a catalog that issues parameterized queries through the control-plane client.
     *
     * @param control JDBC client connected to the control-plane database
     */
    public JdbcTenantCatalog(JdbcClient control) {
        this.control = control;
    }

    /**
     * Resolves an active tenant mapped to a trusted, normalized host name.
     *
     * @param normalizedHost lowercase ASCII host without a port
     * @return resolved tenant, or an empty optional when the mapping is missing or inactive
     */
    @Override
    public Optional<ResolvedTenant> findByTrustedHost(String normalizedHost) {
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

    /**
     * Loads an enabled database descriptor for a tenant and database role.
     *
     * @param key tenant and logical database-role key
     * @return descriptor containing connection metadata, or an empty optional when unavailable
     */
    @Override
    public Optional<TenantDatabaseDescriptor> findDatabase(TenantDataSourceKey key) {
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

    /**
     * Converts a language tag or underscore-separated locale into a Java locale.
     *
     * @param value locale value stored by the control plane, such as {@code id-ID}
     * @return locale containing the parsed language and optional country
     */
    private static Locale parseLocale(String value) {
        String[] parts = value.replace('-', '_').split("_", 3);
        return parts.length == 1 ? Locale.of(parts[0]) : Locale.of(parts[0], parts[1]);
    }
}
