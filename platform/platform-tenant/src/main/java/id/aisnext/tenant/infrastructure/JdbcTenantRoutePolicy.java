package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.RouteOwner;
import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantRouteDecision;
import id.aisnext.tenant.api.TenantRoutePolicy;
import id.aisnext.tenant.api.WriteOwnership;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Reads tenant route-ownership decisions exclusively from the AIS Next control plane. */
public final class JdbcTenantRoutePolicy implements TenantRoutePolicy {
    private final JdbcClient control;

    /**
     * Creates a route policy backed by the isolated control-plane database.
     *
     * @param control JDBC client connected only to the control plane
     */
    public JdbcTenantRoutePolicy(JdbcClient control) {
        this.control = control;
    }

    /**
     * Finds the most-specific route prefix configured for the tenant and request path.
     *
     * @param tenantId tenant resolved from the trusted host
     * @param requestPath normalized absolute servlet request path
     * @return most-specific matching route decision, or empty when unmanaged
     */
    @Override
    public Optional<TenantRouteDecision> findRoute(TenantId tenantId, String requestPath) {
        return control.sql("""
                select r.module_key, r.route_pattern, r.route_owner, r.write_ownership, r.version
                  from tenant_module_route r
                  join tenant t on t.id = r.tenant_id
                 where t.tenant_key = :tenant
                   and (:path = r.route_pattern or :path like r.route_pattern || '/%')
                 order by length(r.route_pattern) desc
                 limit 1
                """)
                .param("tenant", tenantId.value())
                .param("path", requestPath)
                .query((resultSet, rowNumber) -> new TenantRouteDecision(
                        resultSet.getString("module_key"),
                        resultSet.getString("route_pattern"),
                        RouteOwner.valueOf(resultSet.getString("route_owner")),
                        WriteOwnership.valueOf(resultSet.getString("write_ownership")),
                        resultSet.getLong("version")))
                .optional();
    }
}
