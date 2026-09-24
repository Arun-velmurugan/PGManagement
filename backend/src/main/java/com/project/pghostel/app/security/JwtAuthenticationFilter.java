package com.project.pghostel.app.security;

import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserService userService) {

        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // ==========================================
        // GET AUTHORIZATION HEADER
        // ==========================================

        String authHeader =
                request.getHeader("Authorization");

        System.out.println(
                "REQUEST = " + request.getRequestURI()
        );

        System.out.println(
                "AUTH HEADER = " + authHeader
        );

        // ==========================================
        // TOKEN NOT PRESENT
        // ==========================================

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ==========================================
        // GET TOKEN
        // ==========================================

        String token =
                authHeader.substring(7);

        try {

            // ======================================
            // GET USERNAME FROM JWT
            // ======================================

            String username =
                    jwtService.extractUsername(token);

            System.out.println(
                    "JWT USERNAME = " + username
            );

            // ======================================
            // CHECK USERNAME
            // ======================================

            if (username != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                // ==================================
                // FIND USER
                // ==================================

                User user =
                        userService
                                .findByUsername(username)
                                .orElse(null);

                if (user != null) {

                    // ==================================
                    // CHECK USER STATUS
                    // ==================================

                    if (!"ACTIVE".equalsIgnoreCase(
                            user.getStatus())) {

                        System.out.println(
                                "USER IS INACTIVE"
                        );

                        filterChain.doFilter(
                                request,
                                response
                        );

                        return;
                    }

                    // ==================================
                    // GET ROLE
                    // ==================================

                    String role =
                            user.getRole()
                                    .toUpperCase();

                    String authorityName =
                            "ROLE_" + role;

                    SimpleGrantedAuthority authority =
                            new SimpleGrantedAuthority(
                                    authorityName
                            );

                    System.out.println(
                            "DB USERNAME = "
                                    + user.getUsername()
                    );

                    System.out.println(
                            "DB ROLE = "
                                    + user.getRole()
                    );

                    System.out.println(
                            "AUTHORITY = "
                                    + authority.getAuthority()
                    );

                    // ==================================
                    // CREATE AUTHENTICATION
                    // ==================================

                    UsernamePasswordAuthenticationToken
                            authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.getUsername(),
                                    null,
                                    List.of(authority)
                            );

                    // ==================================
                    // SET AUTHENTICATION
                    // ==================================

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );

                    System.out.println(
                            "AUTHENTICATION SET SUCCESSFULLY"
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "JWT ERROR = " + e.getMessage()
            );
        }

        // ==========================================
        // CONTINUE
        // ==========================================

        filterChain.doFilter(
                request,
                response
        );
    }
}