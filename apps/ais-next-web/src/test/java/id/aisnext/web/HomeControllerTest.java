package id.aisnext.web;

import static org.assertj.core.api.Assertions.assertThat;

import id.aisnext.security.api.HandoffPrincipal;
import id.aisnext.tenant.api.ResolvedTenant;
import id.aisnext.tenant.api.TenantId;
import id.aisnext.tenant.api.TenantMode;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;

/** Verifies public entry-page configuration without weakening the handoff boundary. */
class HomeControllerTest {
    /** Creates the public entry-page controller test. */
    HomeControllerTest() {
    }

    /** Confirms that the trusted Legacy login URL is exposed only as landing-page presentation data. */
    @Test
    void exposesConfiguredLegacyLoginUrl() {
        HomeController controller = new HomeController("https://legacy.example.test/login");
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.landing(model);

        assertThat(view).isEqualTo("index");
        assertThat(model.get("legacyLoginUrl")).isEqualTo("https://legacy.example.test/login");
    }

    /** Confirms that the dashboard exposes presentation-safe scalar values from trusted session records. */
    @Test
    void exposesTrustedDashboardPresentationValues() {
        HomeController controller = new HomeController("https://legacy.example.test/login");
        HandoffPrincipal principal = new HandoffPrincipal(new TenantId("uat-local"), "aisnext_uat", "amp");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, "unused-test-credential", List.of());
        ResolvedTenant tenant = new ResolvedTenant(
                new TenantId("uat-local"),
                "AIS UAT Local",
                TenantMode.HYBRID,
                Locale.forLanguageTag("id-ID"),
                ZoneId.of("Asia/Jakarta"));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.dashboard(authentication, tenant, model);

        assertThat(view).isEqualTo("dashboard");
        assertThat(model)
                .containsEntry("userDisplayName", "aisnext_uat")
                .containsEntry("activeRoleId", "amp")
                .containsEntry("tenantDisplayName", "AIS UAT Local")
                .containsEntry("tenantMode", "HYBRID");
    }
}
