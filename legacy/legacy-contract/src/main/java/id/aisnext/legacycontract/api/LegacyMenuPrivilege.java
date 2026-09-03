package id.aisnext.legacycontract.api;

/**
 * Immutable read projection of a legacy menu entry and its role-specific capabilities.
 *
 * @param menuId legacy menu primary key
 * @param label human-readable menu label, normalized to a non-null value
 * @param legacyUrl unchanged legacy navigation target
 * @param parentCode legacy hierarchy root code
 * @param nodeCode legacy hierarchy child code
 * @param readable whether the role may read the feature
 * @param creatable whether the role may create data
 * @param updatable whether the role may update data
 * @param deletable whether the role may delete data
 * @param approvable whether the role may approve data
 * @param rejectable whether the role may reject data
 */
public record LegacyMenuPrivilege(long menuId, String label, String legacyUrl, long parentCode,
                                  long nodeCode, boolean readable, boolean creatable,
                                  boolean updatable, boolean deletable, boolean approvable,
                                  boolean rejectable) {}
