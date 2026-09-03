package id.aisnext;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Enforces Spring Modulith boundaries for the complete AIS Next application. */
class ModularityTest {
    /** Creates the application-module boundary test. */
    ModularityTest() {
    }

    /** Verifies that application modules have no cycles or illegal internal dependencies. */
    @Test
    void applicationModulesHaveNoCyclesOrInternalCrossModuleDependencies() {
        ApplicationModules.of(AisNextApplication.class).verify();
    }
}
