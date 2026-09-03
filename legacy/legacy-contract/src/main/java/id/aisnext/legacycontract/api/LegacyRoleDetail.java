package id.aisnext.legacycontract.api;

import java.util.List;

/**
 * Detailed, immutable role projection including the effective legacy menu privileges.
 *
 * @param id exact legacy role identifier
 * @param name display name with blank legacy names normalized by the adapter
 * @param active whether the legacy role is enabled
 * @param menus ordered menu and privilege projections for the role
 */
public record LegacyRoleDetail(String id, String name, boolean active, List<LegacyMenuPrivilege> menus) {
    /** Creates a role detail and defensively copies its ordered menu list. */
    public LegacyRoleDetail { menus = List.copyOf(menus); }
}
