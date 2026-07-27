package com.dbtraining.reconx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration.
 *
 * TICKET-ADV073 / ADV074
 * - Password encoding using BCrypt
 * - Method-level security support
 * - Stateless security preparation for JWT
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Password encoder used by AuthService/AuthController.
     * Required for user registration and login password verification.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * Main Spring Security filter chain.
     *
     * Current mode:
     * - Development friendly
     * - Swagger/H2 console accessible
     * - JWT rules can be enabled later
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())

                // Needed for H2 console in dev profile
                .headers(headers ->
                        headers.frameOptions(frame ->
                                frame.disable()
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2/**"
                        ).permitAll()

                        // Temporary Day-1 mode
                        .anyRequest().permitAll()
                )

                .build();
    }
}