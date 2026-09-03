package id.aisnext.academic.web;

import static id.aisnext.academic.api.StudentDirectoryAuthorities.READ_STUDENTS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Verifies exact server-derived visibility for the student-directory navigation entry. */
class StudentDirectoryNavigationAdviceTest {
    private final StudentDirectoryNavigationAdvice advice = new StudentDirectoryNavigationAdvice();

    /** Creates the navigation advice test fixture. */
    StudentDirectoryNavigationAdviceTest() {
    }

    /** Confirms the exact menu read authority exposes the navigation entry. */
    @Test
    void allowsExactReadAuthority() {
        assertThat(advice.canRead(authentication(READ_STUDENTS))).isTrue();
    }

    /** Confirms authentication alone cannot expose an unauthorized academic route. */
    @Test
    void rejectsRoleWithoutMenuReadAuthority() {
        assertThat(advice.canRead(authentication("ROLE_MAHA"))).isFalse();
        assertThat(advice.canRead(null)).isFalse();
    }

    /**
     * Creates one authenticated principal with exactly one authority.
     *
     * @param authority authority granted to the fixture principal
     * @return authenticated Spring Security token
     */
    private static UsernamePasswordAuthenticationToken authentication(String authority) {
        return new UsernamePasswordAuthenticationToken(
                "fixture", "unused", List.of(new SimpleGrantedAuthority(authority)));
    }
}
