package id.aisnext.supporting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Verifies literal LIKE semantics for untrusted library item-type filters. */
class JdbcLibraryItemTypeRepositoryTest {
    /** Creates the repository utility test fixture. */
    JdbcLibraryItemTypeRepositoryTest() {
    }

    /** Confirms wildcard and escape characters remain literal after normalization. */
    @Test
    void escapesLikeMetacharacters() {
        assertThat(JdbcLibraryItemTypeRepository.toLikePattern("A%_\\B"))
                .isEqualTo("%a\\%\\_\\\\b%");
    }
}
