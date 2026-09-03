package id.aisnext.config;

import id.aisnext.security.api.NonceStore;
import id.aisnext.security.infrastructure.InMemoryNonceStore;
import id.aisnext.tenant.api.DatabaseRole;
import id.aisnext.tenant.api.ResolvedTenant;
import id.aisnext.tenant.api.TenantCatalog;
import id.aisnext.tenant.api.TenantDataSourceKey;
import id.aisnext.tenant.api.TenantDatabaseDescriptor;
import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantMode;
import id.aisnext.tenant.api.TenantRoutePolicy;
import id.aisnext.tenant.infrastructure.InMemoryTenantCatalog;
import id.aisnext.tenant.infrastructure.InMemoryTenantRoutePolicy;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a non-persistent tenant catalog for isolated development when the control plane is
 * explicitly disabled.
 *
 * <p>Both legacy datasource descriptors are marked read-only. This fallback is not intended for
 * production because nonce replay state is held only in memory.</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "ais.control.enabled", havingValue = "false")
public class LocalCatalogConfiguration {
    /**
     * Creates the Spring configuration definition for non-persistent local infrastructure.
     */
    public LocalCatalogConfiguration() {
    }

    /**
     * Builds the single local tenant and its lazy CORE/FILE database descriptors.
     *
     * @param properties local tenant and database settings
     * @return an immutable in-memory tenant catalog for {@code localhost} and {@code 127.0.0.1}
     */
    @Bean TenantCatalog localTenantCatalog(LocalTenantProperties properties) {
        TenantId id = new TenantId(properties.getTenantKey());
        ResolvedTenant tenant = new ResolvedTenant(id, properties.getDisplayName(), TenantMode.HYBRID,
                Locale.forLanguageTag("id-ID"), ZoneId.of("Asia/Jakarta"));
        TenantDataSourceKey coreKey = new TenantDataSourceKey(id, DatabaseRole.CORE);
        TenantDataSourceKey fileKey = new TenantDataSourceKey(id, DatabaseRole.FILE);
        return new InMemoryTenantCatalog(
                Map.of("localhost", tenant, "127.0.0.1", tenant),
                Map.of(coreKey, new TenantDatabaseDescriptor(coreKey, properties.getCoreJdbcUrl(),
                                properties.getCredentialReference(), properties.getMaximumPoolSize(), true),
                        fileKey, new TenantDatabaseDescriptor(fileKey, properties.getFileJdbcUrl(),
                                properties.getCredentialReference(), properties.getMaximumPoolSize(), true)));
    }

    /**
     * Creates the development-only nonce store used when persistent control state is disabled.
     *
     * @return an in-memory, process-local one-time nonce store
     */
    @Bean NonceStore inMemoryNonceStore() { return new InMemoryNonceStore(); }

    /**
     * Creates local read-only route decisions when persistent control metadata is disabled.
     *
     * @return immutable route policy for the Phase 2 identity prefixes
     */
    @Bean TenantRoutePolicy inMemoryTenantRoutePolicy() {
        return new InMemoryTenantRoutePolicy(PhaseTwoRoutes.localNextReadOnlyDecisions());
    }
}
