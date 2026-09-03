package id.aisnext.attendance.web;

import static id.aisnext.attendance.api.AttendanceAuthorities.READ_DAILY;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Verifies exact server-derived visibility for the attendance navigation entry. */
class AttendanceNavigationAdviceTest {
    private final AttendanceNavigationAdvice advice = new AttendanceNavigationAdvice();

    /** Creates the navigation advice test fixture. */
    AttendanceNavigationAdviceTest() {
    }

    /** Confirms the exact menu read authority exposes the navigation entry. */
    @Test
    void allowsExactReadAuthority() {
        UsernamePasswordAuthenticationToken authentication = authentication(READ_DAILY);

        assertThat(advice.canRead(authentication)).isTrue();
    }

    /** Confirms authentication alone cannot expose an unauthorized attendance route. */
    @Test
    void rejectsRoleWithoutMenuReadAuthority() {
        UsernamePasswordAuthenticationToken authentication = authentication("ROLE_MAHA");

        assertThat(advice.canRead(authentication)).isFalse();
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
