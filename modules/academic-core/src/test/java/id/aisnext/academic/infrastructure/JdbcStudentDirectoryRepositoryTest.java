package id.aisnext.academic.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Verifies literal LIKE semantics for untrusted student-directory filters. */
class JdbcStudentDirectoryRepositoryTest {
    /** Creates the repository utility test fixture. */
    JdbcStudentDirectoryRepositoryTest() {
    }

    /** Confirms wildcard and escape characters remain literal after normalization. */
    @Test
    void escapesLikeMetacharacters() {
        assertThat(JdbcStudentDirectoryRepository.toLikePattern("A%_\\B"))
                .isEqualTo("%a\\%\\_\\\\b%");
    }
}
