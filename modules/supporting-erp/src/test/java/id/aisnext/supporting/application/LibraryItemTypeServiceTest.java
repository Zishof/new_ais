package id.aisnext.supporting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.supporting.domain.LibraryItemTypeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Verifies validation and delegation at the library item-type application boundary. */
class LibraryItemTypeServiceTest {
    private final LibraryItemTypeRepository repository = mock(LibraryItemTypeRepository.class);
    private final LibraryItemTypeService service = new LibraryItemTypeService(repository);

    /** Creates the service test fixture. */
    LibraryItemTypeServiceTest() {
    }

    /** Confirms a normalized page query reaches the read-only persistence port. */
    @Test
    void delegatesNormalizedRequest() {
        service.findItemTypes(1, 25, "  Buku  ");

        ArgumentCaptor<PageQuery> query = ArgumentCaptor.forClass(PageQuery.class);
        verify(repository).findItemTypes(query.capture());
        assertThat(query.getValue()).isEqualTo(new PageQuery(1, 25, "Buku"));
    }

    /** Confirms an oversized response is rejected before repository access. */
    @Test
    void rejectsOversizedPage() {
        assertThatThrownBy(() -> service.findItemTypes(0, 101, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("size must be between 1 and 100");
    }
}
