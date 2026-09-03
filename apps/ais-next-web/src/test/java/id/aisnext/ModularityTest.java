package id.aisnext;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {
    @Test
    void applicationModulesHaveNoCyclesOrInternalCrossModuleDependencies() {
        ApplicationModules.of(AisNextApplication.class).verify();
    }
}
