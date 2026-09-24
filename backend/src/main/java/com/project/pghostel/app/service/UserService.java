package com.project.pghostel.app.service;

import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.repository.TenantRepository;
import com.project.pghostel.app.repository.UserRepository;
import com.project.pghostel.app.entity.Payment;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.repository.PaymentRepository;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

        private final UserRepository userRepository;

        private final PasswordEncoder passwordEncoder;

        private final TenantRepository tenantRepo;

        private final PaymentRepository paymentRepo;
        private final MovementService movementService;

        // ==================================================
        // CONSTRUCTOR
        // ==================================================

        public UserService(
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        TenantRepository tenantRepo,
                        PaymentRepository paymentRepo,
                        MovementService movementService) {

                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.tenantRepo = tenantRepo;
                this.paymentRepo = paymentRepo;
                this.movementService = movementService;
        }

        // ==================================================
        // GET ALL USERS
        // ==================================================

        public List<User> getAllUsers() {

                return userRepository.findAll();
        }

        // ==================================================
        // GET USER BY ID
        // ==================================================

        public Optional<User> findById(Long id) {

                return userRepository.findById(id);
        }

        // ==================================================
        // GET USER BY USERNAME
        // ==================================================

        public Optional<User> findByUsername(String username) {

                return userRepository.findByUsername(username);
        }

        // ==================================================
        // CREATE USER
        // ==================================================
        //
        // Only ADMIN should call this method.
        //
        // New users are always ACTIVE.
        // Password is encrypted before saving.
        //
        // ==================================================

        public User createUser(User user) {
                if (user.getPhone() == null ||
                                !user.getPhone().matches("\\d{10}")) {

                        throw new RuntimeException(
                                        "Phone number must contain exactly 10 digits");
                }

                if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                        throw new RuntimeException("Username already exists");
                }

                if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                        throw new RuntimeException("Email already exists");
                }

                if (userRepository.findByPhone(user.getPhone()).isPresent()) {
                        throw new RuntimeException("Phone number already exists");
                }
                // ------------------------------------------
                // CHECK PASSWORD
                // ------------------------------------------

                if (user.getPassword() == null ||
                                user.getPassword().isBlank()) {

                        throw new RuntimeException(
                                        "Password is required");
                }

                // ------------------------------------------
                // ENCRYPT PASSWORD
                // ------------------------------------------

                user.setPassword(
                                passwordEncoder.encode(
                                                user.getPassword()));

                // ------------------------------------------
                // NEW USER ALWAYS ACTIVE
                // ------------------------------------------

                user.setStatus("ACTIVE");

                // ------------------------------------------
                // SAVE USER
                // ------------------------------------------

                return userRepository.save(user);
        }

        // ==================================================
        // UPDATE USER
        // ==================================================
        //
        // Only ADMIN can update another user.
        //
        // Logged-in ADMIN cannot edit own account.
        //
        // Status is NOT changed here.
        // Status has separate updateStatus() method.
        //
        // ==================================================

        public User updateUser(
                        Long id,
                        User updatedUser,
                        String loggedInUsername,
                        String loggedInRole) {

                // ------------------------------------------
                // ONLY ADMIN ALLOWED
                // ------------------------------------------

                if (!"ADMIN".equalsIgnoreCase(loggedInRole)) {

                        throw new RuntimeException(
                                        "Only ADMIN can update users");
                }

                // ------------------------------------------
                // FIND EXISTING USER
                // ------------------------------------------

                User existingUser = userRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found"));

                // ------------------------------------------
                // PREVENT OWN ACCOUNT EDIT
                // ------------------------------------------

                if (existingUser.getUsername()
                                .equalsIgnoreCase(loggedInUsername)) {

                        throw new RuntimeException(
                                        "You cannot edit your own account");
                }

                // ------------------------------------------
                // CHECK USERNAME
                // ------------------------------------------

                String newUsername = updatedUser.getUsername();

                if (newUsername == null ||
                                newUsername.isBlank()) {

                        throw new RuntimeException(
                                        "Username is required");
                }

                // ------------------------------------------
                // CHECK DUPLICATE USERNAME
                // ------------------------------------------

                if (!existingUser.getUsername()
                                .equalsIgnoreCase(newUsername)
                                &&
                                userRepository.existsByUsername(
                                                newUsername)) {

                        throw new RuntimeException(
                                        "Username already exists");
                }

                // ------------------------------------------
                // UPDATE USERNAME
                // ------------------------------------------

                existingUser.setUsername(
                                newUsername);

                // ------------------------------------------
                // UPDATE FULL NAME
                // ------------------------------------------

                existingUser.setFullName(
                                updatedUser.getFullName());

                // ------------------------------------------
                // UPDATE PHONE
                // ------------------------------------------

                existingUser.setPhone(
                                updatedUser.getPhone());

                // ------------------------------------------
                // UPDATE EMAIL
                // ------------------------------------------

                existingUser.setEmail(
                                updatedUser.getEmail());

                // ------------------------------------------
                // UPDATE ROLE
                // ------------------------------------------

                existingUser.setRole(
                                updatedUser.getRole());

                // ------------------------------------------
                // STATUS NOT UPDATED HERE
                // ------------------------------------------
                //
                // Status can be changed only through
                // updateStatus().
                //
                // ------------------------------------------

                // ------------------------------------------
                // PASSWORD
                // ------------------------------------------
                //
                // If password is provided,
                // encrypt and update it.
                //
                // If empty/null,
                // keep existing password.
                //
                // ------------------------------------------

                if (updatedUser.getPassword() != null &&
                                !updatedUser.getPassword().isBlank()) {

                        existingUser.setPassword(
                                        passwordEncoder.encode(
                                                        updatedUser.getPassword()));
                }

                // ------------------------------------------
                // SAVE
                // ------------------------------------------

                return userRepository.save(existingUser);
        }

        // ==================================================
        // UPDATE USER STATUS
        // ==================================================
        //
        // Only ADMIN can change status.
        //
        // Logged-in ADMIN cannot change own status.
        //
        // Allowed:
        // ACTIVE
        // INACTIVE
        //
        // ==================================================

        @Transactional
        public User updateStatus(
                        Long id,
                        String status,
                        String loggedInUsername,
                        String loggedInRole) {

                // ------------------------------------------
                // ONLY ADMIN ALLOWED
                // ------------------------------------------

                if (!"ADMIN".equalsIgnoreCase(loggedInRole)) {

                        throw new RuntimeException(
                                        "Only ADMIN can change user status");
                }

                // ------------------------------------------
                // FIND USER
                // ------------------------------------------

                User user = userRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found"));

                // ------------------------------------------
                // PREVENT OWN ACCOUNT STATUS CHANGE
                // ------------------------------------------

                if (user.getUsername()
                                .equalsIgnoreCase(loggedInUsername)) {

                        throw new RuntimeException(
                                        "You cannot change your own account status");
                }

                // ------------------------------------------
                // CHECK STATUS
                // ------------------------------------------

                if (status == null ||
                                status.isBlank()) {

                        throw new RuntimeException(
                                        "Status is required");
                }

                // ------------------------------------------
                // ONLY ACTIVE / INACTIVE
                // ------------------------------------------

                if (!"ACTIVE".equalsIgnoreCase(status) &&
                                !"INACTIVE".equalsIgnoreCase(status)) {

                        throw new RuntimeException(
                                        "Invalid status");
                }

                String newStatus = status.toUpperCase();

                // ==================================================
                // TENANT STATUS LOGIC
                // ==================================================

                if ("TENANT".equalsIgnoreCase(user.getRole())) {

                        Tenant tenant = tenantRepo.findByUserId(user.getId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Tenant details not found"));

                        // ==============================================
                        // TENANT → INACTIVE
                        // ==============================================

                        if ("INACTIVE".equals(newStatus)) {

                                List<Payment> payments = paymentRepo.findByTenant_TenantId(
                                                tenant.getTenantId());

                                // ------------------------------------------
                                // CHECK ALL PAYMENT RECORDS
                                // ------------------------------------------

                                boolean allPaid = !payments.isEmpty()
                                                && payments.stream()
                                                                .allMatch(payment -> "PAID".equalsIgnoreCase(
                                                                                payment.getStatus()));

                                // ------------------------------------------
                                // PAYMENT NOT COMPLETED
                                // ------------------------------------------

                                if (!allPaid) {

                                        throw new RuntimeException(
                                                        "Tenant cannot be made inactive. "
                                                                        + "All payment records must be PAID.");
                                }

                                // ------------------------------------------
                                // UPDATE TENANT STATUS
                                // ------------------------------------------

                                tenant.setStatus("INACTIVE");

                                tenantRepo.save(tenant);

                                // ------------------------------------------
                                // MOVEMENT → OUT
                                // ------------------------------------------

                                movementService.markOut(
                                                tenant.getTenantId());
                        }

                        // ==============================================
                        // TENANT → ACTIVE
                        // ==============================================

                        else if ("ACTIVE".equals(newStatus)) {

                                // ------------------------------------------
                                // UPDATE TENANT STATUS
                                // ------------------------------------------

                                tenant.setStatus("ACTIVE");

                                tenantRepo.save(tenant);

                                // ------------------------------------------
                                // MOVEMENT → IN
                                // ------------------------------------------

                                movementService.markIn(
                                                tenant.getTenantId());
                        }
                }

                // ==================================================
                // UPDATE USER STATUS
                // ==================================================

                user.setStatus(newStatus);

                return userRepository.save(user);
        }
        // ==================================================
        // PASSWORD MATCH
        // ==================================================
        //
        // Used during login.
        //
        // rawPassword = password entered in login
        // encodedPassword = BCrypt password from DB
        //
        // ==================================================

        public boolean passwordMatches(
                        String rawPassword,
                        String encodedPassword) {

                return passwordEncoder.matches(
                                rawPassword,
                                encodedPassword);
        }

        // ==================================================
        // DELETE USER
        // ==================================================

        public void deleteUser(
                        Long id,
                        String loggedInUsername,
                        String loggedInRole) {

                // ------------------------------------------
                // ONLY ADMIN ALLOWED
                // ------------------------------------------

                if (!"ADMIN".equalsIgnoreCase(loggedInRole)) {

                        throw new RuntimeException(
                                        "Only ADMIN can delete users");
                }

                // ------------------------------------------
                // FIND USER
                // ------------------------------------------

                User user = userRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found"));

                // ------------------------------------------
                // PREVENT OWN ACCOUNT DELETE
                // ------------------------------------------

                if (user.getUsername()
                                .equalsIgnoreCase(loggedInUsername)) {

                        throw new RuntimeException(
                                        "You cannot delete your own account");
                }

                // ------------------------------------------
                // DELETE
                // ------------------------------------------

                userRepository.delete(user);
        }

        public String resetPassword(
                        String username,
                        String newPassword) {

                // ==============================================
                // FIND USER BY USERNAME
                // ==============================================

                Optional<User> optionalUser = userRepository.findByUsername(username);

                // ==============================================
                // USERNAME NOT FOUND
                // ==============================================

                if (optionalUser.isEmpty()) {

                        throw new RuntimeException(
                                        "Username not found.");

                }

                // ==============================================
                // GET USER
                // ==============================================

                User user = optionalUser.get();

                // ==============================================
                // ENCRYPT NEW PASSWORD
                // ==============================================

                String encryptedPassword = passwordEncoder.encode(
                                newPassword);

                // ==============================================
                // SET NEW PASSWORD
                // ==============================================

                user.setPassword(
                                encryptedPassword);

                // ==============================================
                // SAVE USER
                // ==============================================

                userRepository.save(user);

                // ==============================================
                // SUCCESS
                // ==============================================

                return "Password reset successfully.";

        }

}