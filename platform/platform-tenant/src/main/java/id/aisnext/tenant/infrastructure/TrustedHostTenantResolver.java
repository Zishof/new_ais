package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.ResolvedTenant;
import id.aisnext.tenant.api.TenantCatalog;
import id.aisnext.tenant.api.TenantResolver;
import java.net.IDN;
import java.util.Locale;

/**
 * Resolves tenants only from trusted HTTP host values registered in the tenant catalog.
 *
 * <p>The resolver strips a valid port, handles bracketed IPv6 input, converts internationalized
 * names to ASCII, and applies locale-independent lowercase normalization before catalog lookup.
 * Forwarded-host trust must be established by the web/proxy layer before calling this component.</p>
 */
public final class TrustedHostTenantResolver implements TenantResolver {
    private final TenantCatalog catalog;

    /**
     * Creates a host resolver backed by the supplied tenant catalog.
     *
     * @param catalog catalog of trusted host-to-tenant mappings
     */
    public TrustedHostTenantResolver(TenantCatalog catalog) {
        this.catalog = catalog;
    }

    /**
     * Normalizes a trusted host and returns its active tenant mapping.
     *
     * @param host trusted request host, optionally including a port
     * @return active tenant mapped to the normalized host
     * @throws UnknownTenantException when the host is empty or has no active mapping
     * @throws IllegalArgumentException when the host violates IDN syntax rules
     */
    @Override
    public ResolvedTenant resolveTrustedHost(String host) {
        String normalized = normalize(host);
        return catalog.findByTrustedHost(normalized)
                .orElseThrow(() -> new UnknownTenantException(normalized));
    }

    /**
     * Converts a request host to the canonical form stored by the control plane.
     *
     * @param host host name, bracketed IP literal, or host with a numeric-style port suffix
     * @return lowercase ASCII host without brackets or port
     * @throws UnknownTenantException when the host is null or blank
     * @throws IllegalArgumentException when IDN conversion rejects the value
     */
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
