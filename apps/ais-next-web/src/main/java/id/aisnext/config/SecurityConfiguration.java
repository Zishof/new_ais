package id.aisnext.config;

import id.aisnext.security.api.HandoffTokenService;
import id.aisnext.security.api.NonceStore;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean HandoffTokenService handoffTokenService(HandoffProperties properties, NonceStore nonces) {
        String key = properties.getSigningKey();
        if (key == null || key.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("AIS_HANDOFF_SIGNING_KEY must contain at least 32 UTF-8 bytes");
        }
        return new HandoffTokenService(properties.getIssuer(), properties.getAudience(),
                key.getBytes(StandardCharsets.UTF_8), nonces, Clock.systemUTC());
    }

    @Bean SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/error", "/auth/handoff", "/assets/**", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/openapi.json", "/api/docs/**", "/swagger-ui/**").permitAll()
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
