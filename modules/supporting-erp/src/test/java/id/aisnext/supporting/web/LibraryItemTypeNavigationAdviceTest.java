package id.aisnext.supporting.web;

import static id.aisnext.supporting.api.LibraryItemTypeAuthorities.READ_ITEM_TYPES;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Verifies exact server-derived visibility for the library item-type navigation entry. */
class LibraryItemTypeNavigationAdviceTest {
    private final LibraryItemTypeNavigationAdvice advice = new LibraryItemTypeNavigationAdvice();

    /** Creates the navigation advice test fixture. */
    LibraryItemTypeNavigationAdviceTest() {
    }

    /** Confirms the exact menu read authority exposes the navigation entry. */
    @Test
    void allowsExactReadAuthority() {
        assertThat(advice.canRead(authentication(READ_ITEM_TYPES))).isTrue();
    }

    /** Confirms authentication alone cannot expose an unauthorized Supporting ERP route. */
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
