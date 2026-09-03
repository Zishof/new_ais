package id.aisnext.finance.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Verifies literal LIKE semantics for untrusted account-group filters. */
class JdbcAccountGroupRepositoryTest {
    /** Creates the repository utility test fixture. */
    JdbcAccountGroupRepositoryTest() {
    }

    /** Confirms wildcard and escape characters remain literal after normalization. */
    @Test
    void escapesLikeMetacharacters() {
        assertThat(JdbcAccountGroupRepository.toLikePattern("A%_\\B"))
                .isEqualTo("%a\\%\\_\\\\b%");
    }
}
