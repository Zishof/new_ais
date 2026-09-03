package id.aisnext.legacycontract.api;

/**
 * Compact immutable projection used by the legacy role directory.
 *
 * @param id exact legacy role identifier
 * @param name normalized role display name
 * @param active whether the legacy role is enabled
 */
public record LegacyRoleSummary(String id, String name, boolean active) {}
