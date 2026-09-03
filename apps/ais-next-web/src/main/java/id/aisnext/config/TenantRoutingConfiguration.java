package id.aisnext.config;

import id.aisnext.tenant.api.TenantCatalog;
import id.aisnext.tenant.api.TenantResolver;
import id.aisnext.tenant.api.TenantRoutePolicy;
import id.aisnext.tenant.api.TenantSecretResolver;
import id.aisnext.tenant.infrastructure.CoreRoutingDataSource;
import id.aisnext.tenant.infrastructure.EnvironmentTenantSecretResolver;
import id.aisnext.tenant.infrastructure.FileRoutingDataSource;
import id.aisnext.tenant.infrastructure.TenantDataSourceRegistry;
import id.aisnext.tenant.infrastructure.TrustedHostTenantResolver;
import id.aisnext.websupport.infrastructure.TenantResolutionFilter;
import id.aisnext.websupport.infrastructure.TenantRouteGateFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wires trusted tenant resolution, lazy datasource creation, and role-specific JDBC clients.
 *
 * <p>No tenant connection is opened during startup. A CORE or FILE pool is created only after a
 * trusted host has established {@code TenantContext} for an actual request.</p>
 */
@Configuration(proxyBeanMethods = false)
public class TenantRoutingConfiguration {
    /**
     * Creates the Spring configuration definition for tenant-aware database routing.
     */
    public TenantRoutingConfiguration() {
    }

    /**
     * Resolves database credentials from runtime environment variables.
     *
     * @return secret resolver that never reads credentials from the control-plane schema
     */
    @Bean TenantSecretResolver tenantSecretResolver() {
        return new EnvironmentTenantSecretResolver(System::getenv);
    }

    /**
     * Creates the globally bounded lazy tenant-pool registry.
     *
     * @param catalog tenant and database metadata source
     * @param secrets runtime credential resolver
     * @param properties cache bound and idle TTL
     * @return registry closed by Spring during graceful shutdown
     */
    @Bean(destroyMethod = "close") TenantDataSourceRegistry tenantDataSourceRegistry(
            TenantCatalog catalog, TenantSecretResolver secrets, TenantPoolProperties properties) {
        return new TenantDataSourceRegistry(catalog, secrets, properties.getMaximumCachedPools(), properties.getIdleTtl());
    }

    /**
     * Creates the routing datasource for academic and master-data reads.
     *
     * @param registry lazy tenant-pool registry
     * @return CORE-role routing datasource
     */
    @Bean CoreRoutingDataSource coreRoutingDataSource(TenantDataSourceRegistry registry) {
        return new CoreRoutingDataSource(registry);
    }

    /**
     * Creates the routing datasource for binary/file storage reads.
     *
     * @param registry lazy tenant-pool registry
     * @return FILE-role routing datasource
     */
    @Bean FileRoutingDataSource fileRoutingDataSource(TenantDataSourceRegistry registry) {
        return new FileRoutingDataSource(registry);
    }

    /**
     * Creates a projection-oriented JDBC client routed to the current tenant's CORE database.
     *
     * @param dataSource CORE routing datasource
     * @return tenant-aware CORE JDBC client
     */
    @Bean(name = "coreJdbcClient") JdbcClient coreJdbcClient(CoreRoutingDataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    /**
     * Creates a projection-oriented JDBC client routed to the current tenant's FILE database.
     *
     * @param dataSource FILE routing datasource
     * @return tenant-aware FILE JDBC client
     */
    @Bean(name = "fileJdbcClient") JdbcClient fileJdbcClient(FileRoutingDataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    /**
     * Creates transaction boundaries over the tenant-selected CORE datasource.
     *
     * @param dataSource CORE routing datasource that resolves from trusted tenant context
     * @return transaction manager used by bounded legacy-compatible aggregate writes
     */
    @Bean(name = "coreTransactionManager") PlatformTransactionManager coreTransactionManager(
            CoreRoutingDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * Creates the resolver that accepts only hosts recorded in the tenant catalog.
     *
     * @param catalog trusted tenant-domain source
     * @return host-based tenant resolver
     */
    @Bean TenantResolver tenantResolver(TenantCatalog catalog) { return new TrustedHostTenantResolver(catalog); }

    /**
     * Creates the servlet filter that establishes and clears tenant context per request.
     *
     * @param resolver trusted host resolver
     * @return tenant resolution filter
     */
    @Bean TenantResolutionFilter tenantResolutionFilter(TenantResolver resolver) { return new TenantResolutionFilter(resolver); }

    /**
     * Creates the fail-closed application gate for tenant-specific strangler ownership.
     *
     * @param routePolicy control-plane route-decision source
     * @return route gate protecting all Phase 2 identity prefixes
     */
    @Bean TenantRouteGateFilter tenantRouteGateFilter(TenantRoutePolicy routePolicy) {
        return new TenantRouteGateFilter(routePolicy, PhaseTwoRoutes.governedPrefixes());
    }
}
