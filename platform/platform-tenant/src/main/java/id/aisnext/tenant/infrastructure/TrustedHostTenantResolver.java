package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.ResolvedTenant;
import id.aisnext.tenant.api.TenantCatalog;
import id.aisnext.tenant.api.TenantResolver;
import java.net.IDN;
import java.util.Locale;

public final class TrustedHostTenantResolver implements TenantResolver {
    private final TenantCatalog catalog;

    public TrustedHostTenantResolver(TenantCatalog catalog) {
        this.catalog = catalog;
    }

    @Override public ResolvedTenant resolveTrustedHost(String host) {
        String normalized = normalize(host);
        return catalog.findByTrustedHost(normalized)
                .orElseThrow(() -> new UnknownTenantException(normalized));
    }

    static String normalize(String host) {
        if (host == null || host.isBlank()) throw new UnknownTenantException("<empty>");
        String withoutPort = host.trim();
        if (withoutPort.startsWith("[")) {
            int end = withoutPort.indexOf(']');
            withoutPort = end > 0 ? withoutPort.substring(1, end) : withoutPort;
        } else {
            int colon = withoutPort.lastIndexOf(':');
            if (colon > 0 && withoutPort.indexOf(':') == colon) withoutPort = withoutPort.substring(0, colon);
        }
        return IDN.toASCII(withoutPort, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
    }
}
