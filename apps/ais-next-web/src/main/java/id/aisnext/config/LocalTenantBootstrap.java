package id.aisnext.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Idempotently registers the localhost smoke-test tenant in the AIS Next control plane.
 *
 * <p>The runner writes only control-plane metadata. It records legacy CORE and FILE connections as
 * read-only descriptors and never executes DDL or DML against either legacy database.</p>
 */
@Component
@ConditionalOnProperty(name = {"ais.control.enabled", "ais.tenant.local.bootstrap"}, havingValue = "true", matchIfMissing = true)
public class LocalTenantBootstrap implements ApplicationRunner {
    private final JdbcClient control;
    private final LocalTenantProperties properties;

    /**
     * Creates the bootstrap runner.
     *
     * @param controlJdbcClient JDBC client connected only to the control database
     * @param properties local tenant metadata and legacy connection descriptors
     */
    public LocalTenantBootstrap(JdbcClient controlJdbcClient, LocalTenantProperties properties) {
        this.control = controlJdbcClient;
        this.properties = properties;
    }

    /**
     * Upserts the local tenant, trusted hosts, and read-only CORE/FILE descriptors.
     *
     * @param args application arguments; currently not interpreted
     */
    @Override public void run(ApplicationArguments args) {
        long tenantId = control.sql("""
                insert into tenant (tenant_key, code, name, slug, status, mode, default_locale, timezone)
                values (:key, 'LOCAL', :name, :key, 'ACTIVE', 'HYBRID', 'id_ID', 'Asia/Jakarta')
                on conflict (tenant_key) do update set name = excluded.name
                returning id
                """).param("key", properties.getTenantKey()).param("name", properties.getDisplayName())
                .query(Long.class).single();
        for (String host : new String[] {"localhost", "127.0.0.1"}) {
            control.sql("""
                    insert into tenant_domain (tenant_id, domain, normalized_domain, type, status, primary_domain)
                    values (:tenant, :host, :host, 'SUBDOMAIN', 'ACTIVE', :primary)
                    on conflict (normalized_domain) do update set tenant_id = excluded.tenant_id, status = 'ACTIVE'
                    """).param("tenant", tenantId).param("host", host).param("primary", host.equals("localhost")).update();
        }
        upsertDatabase(tenantId, "CORE", properties.getCoreJdbcUrl());
        upsertDatabase(tenantId, "FILE", properties.getFileJdbcUrl());
    }

    /**
     * Upserts one read-only database descriptor in the control plane.
     *
     * @param tenantId control-plane identifier of the local tenant
     * @param role database role, currently {@code CORE} or {@code FILE}
     * @param jdbcUrl connection URL stored as routing metadata
     */
    private void upsertDatabase(long tenantId, String role, String jdbcUrl) {
        control.sql("""
                insert into tenant_database
                    (tenant_id, database_role, jdbc_url, credential_reference, read_only, maximum_pool_size, enabled)
                values (:tenant, :role, :url, :credential, true, :poolSize, true)
                on conflict (tenant_id, database_role) do update
                    set jdbc_url = excluded.jdbc_url,
                        credential_reference = excluded.credential_reference,
                        read_only = true,
                        maximum_pool_size = excluded.maximum_pool_size,
                        enabled = true
                """).param("tenant", tenantId).param("role", role).param("url", jdbcUrl)
                .param("credential", properties.getCredentialReference()).param("poolSize", properties.getMaximumPoolSize())
                .update();
    }
}
