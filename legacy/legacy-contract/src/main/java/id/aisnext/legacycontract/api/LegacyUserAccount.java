package id.aisnext.legacycontract.api;

import java.util.List;

/**
 * Immutable read projection of the identity fields needed by the handoff bridge.
 *
 * <p>No password or password hash is exposed through this contract.</p>
 *
 * @param userId exact legacy user identifier
 * @param displayName normalized human-readable name
 * @param active whether the legacy account is enabled
 * @param primaryRoleId primary legacy role identifier
 * @param roleIds distinct, ordered role identifiers assigned to the account
 */
public record LegacyUserAccount(String userId, String displayName, boolean active,
                                String primaryRoleId, List<String> roleIds) {
    /** Creates an account projection and defensively copies the assigned-role list. */
    public LegacyUserAccount { roleIds = List.copyOf(roleIds); }
}
