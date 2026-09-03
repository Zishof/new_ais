package id.aisnext.legacycontract.api;

/** Read-only contract for consulting legacy-to-Next API compatibility metadata. */
public interface LegacyApiFacade {
    /**
     * Captures the migration status of one legacy API contract.
     *
     * @param legacyContract exact identifier of the existing contract
     * @param replacementContract replacement endpoint identifier, or {@code null} when unknown
     * @param status compatibility lifecycle status
     */
    record CompatibilityDecision(String legacyContract, String replacementContract, String status) {}

    /**
     * Looks up the migration decision for a legacy contract.
     *
     * @param legacyContract exact identifier of the existing contract
     * @return current compatibility decision; never {@code null}
     */
    CompatibilityDecision describe(String legacyContract);
}
