package id.aisnext.tenant.infrastructure;

import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantWriteDecision;
import id.aisnext.tenant.api.TenantWritePolicy;
import id.aisnext.tenant.api.WriteOwnership;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Reads aggregate write ownership exclusively from the isolated AIS Next control plane. */
public final class JdbcTenantWritePolicy implements TenantWritePolicy {
    private final JdbcClient control;

    /**
     * Creates a write policy backed by control-plane metadata.
     *
     * @param control JDBC client connected only to {@code ais_next_control}
     */
    public JdbcTenantWritePolicy(JdbcClient control) {
        this.control = control;
    }

    /**
     * Finds the exact tenant/aggregate ownership row without consulting tenant data databases.
     *
     * @param tenantId trusted tenant identifier
     * @param aggregateKey stable aggregate key
     * @return current ownership decision, or empty when unregistered
     */
    @Override
    public Optional<TenantWriteDecision> findWriteDecision(TenantId tenantId, String aggregateKey) {
        return control.sql("""
                select s.aggregate_key, s.write_ownership, s.version
                  from tenant_migration_state s
                  join tenant t on t.id = s.tenant_id
                 where t.tenant_key = :tenant and s.aggregate_key = :aggregate
                """)
                .param("tenant", tenantId.value())
                .param("aggregate", aggregateKey)
                .query((resultSet, rowNumber) -> new TenantWriteDecision(
                        resultSet.getString("aggregate_key"),
                        WriteOwnership.valueOf(resultSet.getString("write_ownership")),
                        resultSet.getLong("version")))
                .optional();
    }
}
