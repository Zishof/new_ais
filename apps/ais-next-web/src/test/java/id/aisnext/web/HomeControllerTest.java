package id.aisnext.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
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
}
