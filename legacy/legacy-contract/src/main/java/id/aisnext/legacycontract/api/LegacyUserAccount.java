package id.aisnext.legacycontract.api;

import java.util.List;

public record LegacyUserAccount(String userId, String displayName, boolean active,
                                String primaryRoleId, List<String> roleIds) {
    public LegacyUserAccount { roleIds = List.copyOf(roleIds); }
}
