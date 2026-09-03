package id.aisnext.legacyapi.application;

import id.aisnext.legacycontract.api.LegacyApiFacade;
import java.util.Map;

public final class DocumentedLegacyApiFacade implements LegacyApiFacade {
    private final Map<String, String> replacements;

    public DocumentedLegacyApiFacade(Map<String, String> replacements) { this.replacements = Map.copyOf(replacements); }

    @Override public CompatibilityDecision describe(String legacyContract) {
        String replacement = replacements.get(legacyContract);
        return replacement == null
                ? new CompatibilityDecision(legacyContract, null, "INVENTORY_REQUIRED")
                : new CompatibilityDecision(legacyContract, replacement, "PARALLEL");
    }
}
