package id.aisnext.legacycontract.api;

public interface LegacyApiFacade {
    record CompatibilityDecision(String legacyContract, String replacementContract, String status) {}
    CompatibilityDecision describe(String legacyContract);
}
