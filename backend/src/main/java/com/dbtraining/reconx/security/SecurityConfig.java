package com.dbtraining.reconx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
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
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {

         http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .headers(headers ->
                        headers.frameOptions(frame -> frame.disable())
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/auth/login",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/v1/trades/**")
                        .hasAnyRole("VIEWER", "TRADER", "RECON_ANALYST", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/v1/trades")
                        .hasAnyRole("TRADER", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/v1/trades/**")
                        .hasAnyRole("TRADER", "ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/v1/trades/**")
                        .hasAnyRole("TRADER", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/v1/trades/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/v1/recon/**")
                        .hasAnyRole("RECON_ANALYST", "ADMIN")

                        .requestMatchers("/v1/audit/**")
                        .hasAnyRole("RECON_ANALYST", "ADMIN")

                        .anyRequest().authenticated()
                )
                 .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                 .exceptionHandling(exception ->
                         exception.authenticationEntryPoint(
                                 (request, response, authException) ->
                                         response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                         )
                 );

        return http.build();
    }
}