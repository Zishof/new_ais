package id.aisnext.legacycontract.api;

import java.util.List;

public record LegacyRoleDetail(String id, String name, boolean active, List<LegacyMenuPrivilege> menus) {
    public LegacyRoleDetail { menus = List.copyOf(menus); }
}
