package com.project.pghostel.app.controller;

import com.project.pghostel.app.dto.ResetPasswordRequest;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.security.JwtService;
import com.project.pghostel.app.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

        private final UserService userService;

        private final JwtService jwtService;

        // ==================================================
        // CONSTRUCTOR
        // ==================================================

        public UserController(
                        UserService userService,
                        JwtService jwtService) {

                this.userService = userService;
                this.jwtService = jwtService;
        }

        // ==================================================
        // GET ALL USERS
        // ADMIN ONLY
        // ==================================================

        @GetMapping
        public ResponseEntity<?> getAllUsers(
                        Authentication authentication) {

                System.out.println("CONTROLLER AUTH = " + authentication);
                System.out.println("CONTROLLER NAME = "
                                + authentication.getName());
                System.out.println("CONTROLLER AUTHORITIES = "
                                + authentication.getAuthorities());

                try {

                        checkAdmin(authentication);

                        List<User> users = userService.getAllUsers();

                        return ResponseEntity.ok(users);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .status(403)
                                        .body(e.getMessage());
                }
        }

        // ==================================================
        // GET USER BY ID
        // ADMIN ONLY
        // ==================================================

        @GetMapping("/{id}")
        public ResponseEntity<?> getUserById(
                        @PathVariable Long id,
                        Authentication authentication) {

                try {

                        checkAdmin(authentication);

                        return userService.findById(id)
                                        .map(user -> ResponseEntity.ok(user))
                                        .orElse(
                                                        ResponseEntity
                                                                        .notFound()
                                                                        .build());

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .status(403)
                                        .body(e.getMessage());
                }
        }

        // ==================================================
        // CREATE USER
        // ADMIN ONLY
        // ==================================================
        //
        // New user status is automatically ACTIVE
        // Password is encrypted in UserService
        //
        // ==================================================

        @PostMapping
        public ResponseEntity<?> createUser(
                        @RequestBody User user,
                        Authentication authentication) {

                try {

                        checkAdmin(authentication);

                        User savedUser = userService.createUser(user);

                        return ResponseEntity.ok(savedUser);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());
                }
        }

        // ==================================================
        // UPDATE USER
        // ADMIN ONLY
        // OWN ACCOUNT CANNOT BE EDITED
        // ==================================================

        @PutMapping("/{id}")
        public ResponseEntity<?> updateUser(
                        @PathVariable Long id,
                        @RequestBody User user,
                        Authentication authentication) {

                try {

                        checkAdmin(authentication);

                        // Logged-in username
                        String loggedInUsername = authentication.getName();

                        // Logged-in role
                        String loggedInRole = getLoggedInRole(authentication);

                        User updatedUser = userService.updateUser(
                                        id,
                                        user,
                                        loggedInUsername,
                                        loggedInRole);

                        return ResponseEntity.ok(updatedUser);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());
                }
        }

        // ==================================================
        // UPDATE USER STATUS
        // ADMIN ONLY
        // OWN ACCOUNT CANNOT BE CHANGED
        // ==================================================

        @PutMapping("/{id}/status")
        public ResponseEntity<?> updateStatus(
                        @PathVariable Long id,
                        @RequestParam String status,
                        Authentication authentication) {

                try {

                        checkAdmin(authentication);

                        // Logged-in username
                        String loggedInUsername = authentication.getName();

                        // Logged-in role
                        String loggedInRole = getLoggedInRole(authentication);

                        User updatedUser = userService.updateStatus(
                                        id,
                                        status,
                                        loggedInUsername,
                                        loggedInRole);

                        return ResponseEntity.ok(updatedUser);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());
                }
        }

        // ==================================================
        // LOGIN
        // PUBLIC
        // ==================================================

        @PostMapping("/login")
        public ResponseEntity<?> login(
                        @RequestBody User loginUser) {

                // ==================================================
                // FIND USER
                // ==================================================

                User user = userService
                                .findByUsername(
                                                loginUser.getUsername())
                                .orElse(null);

                // ==================================================
                // USER NOT FOUND
                // ==================================================

                if (user == null) {

                        return ResponseEntity
                                        .status(401)
                                        .body(
                                                        "Invalid username or password");
                }

                // ==================================================
                // PASSWORD CHECK
                // ==================================================

                if (!userService.passwordMatches(
                                loginUser.getPassword(),
                                user.getPassword())) {

                        return ResponseEntity
                                        .status(401)
                                        .body(
                                                        "Invalid username or password");
                }

                // ==================================================
                // CHECK ACCOUNT STATUS
                // ==================================================

                if (!"ACTIVE".equalsIgnoreCase(
                                user.getStatus())) {

                        return ResponseEntity
                                        .status(403)
                                        .body(
                                                        "User account is inactive");
                }

                // ==================================================
                // GENERATE JWT
                // ==================================================

                String token = jwtService.generateToken(
                                user.getUsername(),
                                user.getRole());

                // ==================================================
                // LOGIN RESPONSE
                // ==================================================

                Map<String, Object> response = new HashMap<>();

                response.put(
                                "username",
                                user.getUsername());

                response.put(
                                "role",
                                user.getRole());

                response.put(
                                "token",
                                token);

                return ResponseEntity.ok(response);
        }

        // ==================================================
        // CHECK ADMIN
        // ==================================================
        //
        // Only ADMIN can access User Management
        //
        // ==================================================

        private void checkAdmin(
                        Authentication authentication) {

                if (authentication == null ||
                                !authentication.isAuthenticated()) {

                        throw new RuntimeException(
                                        "Authentication required");
                }

                boolean isAdmin = authentication
                                .getAuthorities()
                                .stream()
                                .anyMatch(authority -> authority
                                                .getAuthority()
                                                .equals("ROLE_ADMIN"));

                if (!isAdmin) {

                        throw new RuntimeException(
                                        "Only ADMIN can perform this action");
                }
        }

        // ==================================================
        // GET LOGGED-IN ROLE
        // ==================================================

        private String getLoggedInRole(
                        Authentication authentication) {

                return authentication
                                .getAuthorities()
                                .stream()
                                .findFirst()
                                .map(authority -> authority
                                                .getAuthority()
                                                .replace(
                                                                "ROLE_",
                                                                ""))
                                .orElse("");
        }

        @PutMapping("/reset-password")
public ResponseEntity<?> resetPassword(
        @RequestBody ResetPasswordRequest request) {

    try {

        // ==========================================
        // VALIDATION
        // ==========================================

        if (request.getUsername() == null ||
                request.getUsername().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Please enter username.");
        }


        if (request.getPassword() == null ||
                request.getPassword().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Please enter new password.");
        }


        // ==========================================
        // RESET PASSWORD
        // ==========================================

        String message =
                userService.resetPassword(
                        request.getUsername().trim(),
                        request.getPassword()
                );


        // ==========================================
        // SUCCESS
        // ==========================================

        return ResponseEntity.ok(message);


    } catch (RuntimeException e) {

        // ==========================================
        // USERNAME NOT FOUND
        // ==========================================

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());


    } catch (Exception e) {

        return ResponseEntity
                .badRequest()
                .body("Unable to reset password.");

    }

}
}