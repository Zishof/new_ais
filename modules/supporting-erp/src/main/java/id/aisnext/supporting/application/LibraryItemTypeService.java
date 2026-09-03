package id.aisnext.supporting.application;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.supporting.api.LibraryItemTypeEntry;
import id.aisnext.supporting.domain.LibraryItemTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application boundary for the read-only global library item-type directory. */
@Service
public class LibraryItemTypeService {
    /** Maximum number of item-type rows accepted by one browser or API request. */
    public static final int MAXIMUM_PAGE_SIZE = 100;

    private final LibraryItemTypeRepository repository;

    /**
     * Creates the item-type directory service.
     *
     * @param repository tenant-aware, read-only item-type projection
     */
    public LibraryItemTypeService(LibraryItemTypeRepository repository) {
        this.repository = repository;
    }

    /**
     * Validates untrusted request bounds and returns one deterministic item-type page.
     *
     * @param page zero-based page index
     * @param size requested page size from 1 through 100
     * @param filter case-insensitive literal name-or-description fragment
     * @return immutable page containing only fields approved by the Phase 6 contract
     * @throws IllegalArgumentException when paging is outside its bounds
     */
    @Transactional(transactionManager = "coreTransactionManager", readOnly = true)
    public PageResult<LibraryItemTypeEntry> findItemTypes(int page, int size, String filter) {
        if (size > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        return repository.findItemTypes(new PageQuery(page, size, filter));
    }
}
