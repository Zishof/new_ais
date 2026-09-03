package id.aisnext.supporting.api;

/**
 * Immutable, data-minimized view of one global legacy library item type.
 *
 * @param itemTypeId legacy item-type primary key
 * @param name trimmed item-type display name
 * @param description normalized description, or blank when absent
 */
public record LibraryItemTypeEntry(long itemTypeId, String name, String description) {
}
