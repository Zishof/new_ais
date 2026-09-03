package id.aisnext.security.infrastructure;

import id.aisnext.security.api.NonceStore;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Cluster-safe nonce store backed by the AIS Next control-plane database.
 *
 * <p>The database uniqueness constraint makes consumption atomic, and only a SHA-256 digest of the
 * nonce is persisted.</p>
 */
public final class JdbcNonceStore implements NonceStore {
    private final JdbcClient control;

    /**
     * Creates a persistent nonce adapter.
     *
     * @param control JDBC client bound only to the AIS Next control database
     */
    public JdbcNonceStore(JdbcClient control) { this.control = control; }

    /**
     * Inserts the issuer and nonce digest unless that pair has already been consumed.
     *
     * @param issuer issuer namespace stored in clear text
     * @param nonce secret nonce hashed inside PostgreSQL before storage
     * @param expiresAt time after which the row can be purged
     * @return {@code true} when one row was inserted; {@code false} on replay
     */
    @Override public boolean consumeOnce(String issuer, String nonce, Instant expiresAt) {
        return control.sql("""
                insert into security_handoff_nonce (issuer, nonce_hash, expires_at)
                values (:issuer, encode(sha256(convert_to(:nonce, 'UTF8')), 'hex'), :expires)
                on conflict (issuer, nonce_hash) do nothing
                """)
                .param("issuer", issuer)
                .param("nonce", nonce)
                .param("expires", Timestamp.from(expiresAt))
                .update() == 1;
    }
}
