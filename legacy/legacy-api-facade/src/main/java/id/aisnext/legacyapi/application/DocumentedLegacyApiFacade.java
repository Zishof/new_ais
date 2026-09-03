package id.aisnext.legacyapi.application;

import id.aisnext.legacycontract.api.LegacyApiFacade;
import java.util.Map;

/**
 * Describes coexistence mappings between inventoried legacy contracts and AIS Next replacements.
 *
 * <p>This facade is metadata-only: it does not proxy or mutate a legacy endpoint.</p>
 */
public final class DocumentedLegacyApiFacade implements LegacyApiFacade {
    private final Map<String, String> replacements;

    /**
     * Creates an immutable compatibility catalog.
     *
     * @param replacements mapping from exact legacy contract identifiers to versioned replacements
     */
    public DocumentedLegacyApiFacade(Map<String, String> replacements) { this.replacements = Map.copyOf(replacements); }

    /**
     * Describes whether a legacy contract has an identified parallel replacement.
     *
     * @param legacyContract exact inventoried legacy contract identifier
     * @return compatibility decision with status {@code PARALLEL} or {@code INVENTORY_REQUIRED}
     */
    @Override public CompatibilityDecision describe(String legacyContract) {
        String replacement = replacements.get(legacyContract);
        return replacement == null
                ? new CompatibilityDecision(legacyContract, null, "INVENTORY_REQUIRED")
                : new CompatibilityDecision(legacyContract, replacement, "PARALLEL");
    }
}
