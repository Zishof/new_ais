package id.aisnext.finance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import id.aisnext.finance.domain.AccountGroupRepository;
import id.aisnext.kernel.api.PageQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Verifies validation and delegation at the account-group application boundary. */
class AccountGroupServiceTest {
    private final AccountGroupRepository repository = mock(AccountGroupRepository.class);
    private final AccountGroupService service = new AccountGroupService(repository);

    /** Creates the service test fixture. */
    AccountGroupServiceTest() {
    }

    /** Confirms a normalized page query reaches the read-only persistence port. */
    @Test
    void delegatesNormalizedRequest() {
        service.findAccountGroups(1, 25, "  Aset  ");

        ArgumentCaptor<PageQuery> query = ArgumentCaptor.forClass(PageQuery.class);
        verify(repository).findAccountGroups(query.capture());
        assertThat(query.getValue()).isEqualTo(new PageQuery(1, 25, "Aset"));
    }

    /** Confirms an oversized response is rejected before repository access. */
    @Test
    void rejectsOversizedPage() {
        assertThatThrownBy(() -> service.findAccountGroups(0, 101, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("size must be between 1 and 100");
    }
}
