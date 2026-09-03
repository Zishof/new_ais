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
import id.aisnext.tenant.infrastructure.InMemoryTenantCatalog;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "ais.control.enabled", havingValue = "false")
public class LocalCatalogConfiguration {
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

    @Bean NonceStore inMemoryNonceStore() { return new InMemoryNonceStore(); }
}
