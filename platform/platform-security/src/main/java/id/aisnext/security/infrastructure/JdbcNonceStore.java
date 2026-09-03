package id.aisnext.security.infrastructure;

import id.aisnext.security.api.NonceStore;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class JdbcNonceStore implements NonceStore {
    private final JdbcClient control;

    public JdbcNonceStore(JdbcClient control) { this.control = control; }

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
