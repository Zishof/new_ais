package id.aisnext.finance.api;

/**
 * Immutable, data-minimized view of one global legacy financial account group.
 *
 * @param accountGroupId legacy account-group primary key
 * @param name trimmed account-group display name
 * @param description normalized description, or blank when absent
 */
public record AccountGroupEntry(long accountGroupId, String name, String description) {
}
