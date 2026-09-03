package id.aisnext.config;

import id.aisnext.tenant.api.TenantCatalog;
import id.aisnext.tenant.api.TenantResolver;
import id.aisnext.tenant.api.TenantSecretResolver;
import id.aisnext.tenant.infrastructure.CoreRoutingDataSource;
import id.aisnext.tenant.infrastructure.EnvironmentTenantSecretResolver;
import id.aisnext.tenant.infrastructure.FileRoutingDataSource;
import id.aisnext.tenant.infrastructure.TenantDataSourceRegistry;
import id.aisnext.tenant.infrastructure.TrustedHostTenantResolver;
import id.aisnext.websupport.infrastructure.TenantResolutionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration(proxyBeanMethods = false)
public class TenantRoutingConfiguration {
    @Bean TenantSecretResolver tenantSecretResolver() {
        return new EnvironmentTenantSecretResolver(System::getenv);
    }

    @Bean(destroyMethod = "close") TenantDataSourceRegistry tenantDataSourceRegistry(
            TenantCatalog catalog, TenantSecretResolver secrets, TenantPoolProperties properties) {
        return new TenantDataSourceRegistry(catalog, secrets, properties.getMaximumCachedPools(), properties.getIdleTtl());
    }

    @Bean CoreRoutingDataSource coreRoutingDataSource(TenantDataSourceRegistry registry) {
        return new CoreRoutingDataSource(registry);
    }

    @Bean FileRoutingDataSource fileRoutingDataSource(TenantDataSourceRegistry registry) {
        return new FileRoutingDataSource(registry);
    }

    @Bean(name = "coreJdbcClient") JdbcClient coreJdbcClient(CoreRoutingDataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean(name = "fileJdbcClient") JdbcClient fileJdbcClient(FileRoutingDataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean TenantResolver tenantResolver(TenantCatalog catalog) { return new TrustedHostTenantResolver(catalog); }
    @Bean TenantResolutionFilter tenantResolutionFilter(TenantResolver resolver) { return new TenantResolutionFilter(resolver); }
}
