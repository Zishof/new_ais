package id.aisnext.config;

import static id.aisnext.academic.api.StudentDirectoryAuthorities.READ_STUDENTS;
import static id.aisnext.identity.application.HandoffAuthorizationService.ROLE_DIRECTORY_READ_AUTHORITY;
import static id.aisnext.attendance.api.AttendanceAuthorities.READ_DAILY;
import static id.aisnext.organization.api.SchoolTypeAuthorities.CREATE;
import static id.aisnext.organization.api.SchoolTypeAuthorities.DELETE;
import static id.aisnext.organization.api.SchoolTypeAuthorities.READ;
import static id.aisnext.organization.api.SchoolTypeAuthorities.UPDATE;

import id.aisnext.security.api.HandoffTokenService;
import id.aisnext.security.api.NonceStore;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Defines AIS Next authentication, authorization, session persistence, and browser security
 * headers.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfiguration {
    /**
     * Creates the Spring configuration definition for authentication and browser security.
     */
    public SecurityConfiguration() {
    }

    /**
     * Creates the verifier for signed handoff tokens.
     *
     * @param properties expected issuer, audience, and runtime signing key
     * @param nonces one-time-use nonce store
     * @return an HMAC-SHA256 handoff-token service using the system UTC clock
     * @throws IllegalStateException when the configured key is shorter than 256 bits in UTF-8
     */
    @Bean HandoffTokenService handoffTokenService(HandoffProperties properties, NonceStore nonces) {
        String key = properties.getSigningKey();
        if (key == null || key.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("AIS_HANDOFF_SIGNING_KEY must contain at least 32 UTF-8 bytes");
        }
        return new HandoffTokenService(properties.getIssuer(), properties.getAudience(),
                key.getBytes(StandardCharsets.UTF_8), nonces, Clock.systemUTC());
    }

    /**
     * Stores authenticated security contexts in the rotated HTTP session created at handoff.
     *
     * @return the session-backed security-context repository
     */
    @Bean SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Configures public endpoints, authenticated application routes, logout, and restrictive
     * browser headers.
     *
     * @param http mutable Spring Security HTTP configuration
     * @return the immutable security filter chain
     * @throws Exception if Spring Security cannot build the chain
     */
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/error", "/auth/handoff", "/assets/**", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/openapi.json", "/api/docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/school-types/new").hasAuthority(CREATE)
                        .requestMatchers(HttpMethod.GET, "/school-types/*/edit").hasAuthority(UPDATE)
                        .requestMatchers(HttpMethod.POST, "/school-types/import",
                                "/api/v1/school-types/import").authenticated()
                        .requestMatchers(HttpMethod.POST, "/school-types").hasAuthority(CREATE)
                        .requestMatchers(HttpMethod.POST, "/school-types/*/delete").hasAuthority(DELETE)
                        .requestMatchers(HttpMethod.POST, "/school-types/*").hasAuthority(UPDATE)
                        .requestMatchers(HttpMethod.GET, "/school-types/**", "/api/v1/school-types/**")
                        .hasAuthority(READ)
                        .requestMatchers(HttpMethod.POST, "/api/v1/school-types").hasAuthority(CREATE)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/school-types/*").hasAuthority(UPDATE)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/school-types/*").hasAuthority(DELETE)
                        .requestMatchers("/school-types/**", "/api/v1/school-types/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/attendance/daily",
                                "/api/v1/attendance/daily").hasAuthority(READ_DAILY)
                        .requestMatchers("/attendance/**", "/api/v1/attendance/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/academic/students",
                                "/api/v1/academic/students").hasAuthority(READ_STUDENTS)
                        .requestMatchers("/academic/**", "/api/v1/academic/**").denyAll()
                        .requestMatchers("/roles/**", "/api/v1/roles/**", "/search", "/api/v1/search")
                        .hasAuthority(ROLE_DIRECTORY_READ_AUTHORITY)
                        .anyRequest().authenticated())
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'"))
                        .frameOptions(frame -> frame.deny()))
                .requestCache(cache -> cache.disable());
        return http.build();
    }
}
