package id.aisnext.supporting.domain;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.supporting.api.LibraryItemTypeEntry;

/** Read-only persistence port for the audited global library item-type projection. */
@FunctionalInterface
public interface LibraryItemTypeRepository {
    /**
     * Returns one deterministic page containing only approved item-type fields.
     *
     * @param query validated page and literal text filter
     * @return page ordered by item-type name and legacy identifier
     */
    PageResult<LibraryItemTypeEntry> findItemTypes(PageQuery query);
}
