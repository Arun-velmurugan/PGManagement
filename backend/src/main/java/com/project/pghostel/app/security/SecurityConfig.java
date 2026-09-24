package com.project.pghostel.app.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ==========================================
    // PASSWORD ENCODER
    // ==========================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // ==========================================
    // CORS
    // Frontend is now deployed separately from the
    // backend, so cross-origin requests must be
    // explicitly allowed here.
    // ==========================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Add every origin the frontend is served from
        // (local dev servers, deployed frontend URL, etc.)
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "http://localhost:3000",
            "http://localhost:8081"
        ));

        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


    // ==========================================
    // SECURITY FILTER CHAIN
    // ==========================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http

            // Enable CORS using the bean above
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Disable CSRF
            .csrf(csrf -> csrf.disable())

            // JWT = Stateless
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            // ======================================
            // AUTHORIZATION
            // ======================================

            .authorizeHttpRequests(auth -> auth

                // ----------------------------------
                // FRONTEND FILES
                // ----------------------------------

                .requestMatchers(
                    "/html/**",
                    "/css/**",
                    "/js/**",
                    "/favicon.ico"
                ).permitAll()


                // ----------------------------------
                // LOGIN + REGISTER
                // ----------------------------------

                .requestMatchers(
                    "/api/users/login",
                    "/api/users/register",
                    "/api/users/reset-password"
                ).permitAll()


                // ----------------------------------
                // USER MANAGEMENT
                // ADMIN ONLY
                // ----------------------------------

                .requestMatchers(
                    "/api/users/**"
                ).hasRole("ADMIN")


                // ----------------------------------
                // OTHER API
                // ----------------------------------

                .anyRequest().authenticated()
            )


            // ======================================
            // JWT FILTER
            // ======================================

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}