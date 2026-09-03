package id.aisnext.security.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import id.aisnext.security.infrastructure.InMemoryNonceStore;
import id.aisnext.tenant.api.TenantId;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class HandoffTokenServiceTest {
    private final Instant now = Instant.parse("2026-09-03T12:00:00Z");
    private final HandoffTokenService service = new HandoffTokenService("ais-legacy", "ais-next",
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8),
            new InMemoryNonceStore(Clock.fixed(now, ZoneOffset.UTC)), Clock.fixed(now, ZoneOffset.UTC));

    @Test void verifiesExpectedClaimsAndConsumesNonceOnce() {
        String token = service.issue(new HandoffClaims("ais-legacy", "ais-next", new TenantId("tenant-a"),
                "user-1", "Akademik", "nonce-1", now.plusSeconds(60)));
        assertThat(service.verifyAndConsume(token)).isEqualTo(
                new HandoffPrincipal(new TenantId("tenant-a"), "user-1", "Akademik"));
        assertThatThrownBy(() -> service.verifyAndConsume(token)).isInstanceOf(InvalidHandoffTokenException.class);
    }

    @Test void rejectsExpiredOrModifiedTokens() {
        String expired = service.issue(new HandoffClaims("ais-legacy", "ais-next", new TenantId("tenant-a"),
                "user-1", "Akademik", "nonce-expired", now.minusSeconds(1)));
        assertThatThrownBy(() -> service.verifyAndConsume(expired)).isInstanceOf(InvalidHandoffTokenException.class);
        assertThatThrownBy(() -> service.verifyAndConsume(expired + "x")).isInstanceOf(InvalidHandoffTokenException.class);
    }
}
