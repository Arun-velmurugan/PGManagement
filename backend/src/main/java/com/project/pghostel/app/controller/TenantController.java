package com.project.pghostel.app.controller;

import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.service.TenantService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
@CrossOrigin
public class TenantController {

        private final TenantService tenantService;

        public TenantController(TenantService tenantService) {
                this.tenantService = tenantService;
        }

        // ==================================================
        // GET ALL TENANTS
        // ==================================================

        @GetMapping
        public List<Tenant> getAllTenants() {

                return tenantService.getAllTenants();
        }

        // ==================================================
        // GET TENANT BY ID
        // ==================================================

        @GetMapping("/{id}")
        public Tenant getTenant(
                        @PathVariable Long id) {

                return tenantService.getTenantById(id);
        }

        // ==================================================
        // GET TENANT HISTORY
        // ==================================================

        @GetMapping("/{id}/history")
        public ResponseEntity<?> getTenantHistory(
                        @PathVariable Long id) {

                try {

                        return ResponseEntity.ok(
                                        tenantService.getTenantHistory(id));

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(Map.of(
                                                        "message",
                                                        e.getMessage()));
                }
        }

        // ==================================================
        // GET AVAILABLE TENANT USERS
        // ==================================================

        @GetMapping("/available-users")
        public List<User> getAvailableTenantUsers() {

                return tenantService.getAvailableTenantUsers();
        }

        // ==================================================
        // ADD TENANT
        // ==================================================

        @PostMapping
        public ResponseEntity<?> createTenant(
                        @RequestBody Map<String, Object> request) {

                try {

                        Long userId = Long.valueOf(
                                        request.get("userId").toString());

                        Long roomId = Long.valueOf(
                                        request.get("roomId").toString());

                        String foodPlan =
                                        request.get("foodPlan").toString();

                        LocalDate checkInDate = null;

                        if (request.get("checkInDate") != null
                                        && !request.get("checkInDate")
                                                        .toString()
                                                        .isBlank()) {

                                checkInDate = LocalDate.parse(
                                                request.get("checkInDate")
                                                                .toString());
                        }

                        Tenant tenant = tenantService.createTenant(
                                        userId,
                                        roomId,
                                        foodPlan,
                                        checkInDate);

                        return ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(tenant);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(Map.of(
                                                        "message",
                                                        e.getMessage()));
                }
        }

        // ==================================================
        // MAKE TENANT INACTIVE
        // ==================================================

        @PutMapping("/{id}/inactive")
        public ResponseEntity<?> makeInactive(
                        @PathVariable Long id) {

                try {

                        tenantService.makeTenantInactive(id);

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "message",
                                                        "Tenant made inactive successfully."));

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(Map.of(
                                                        "message",
                                                        e.getMessage()));
                }
        }

        // ==================================================
        // MAKE TENANT ACTIVE / REJOIN
        // ==================================================

        @PutMapping("/{id}/active")
        public ResponseEntity<?> makeActive(
                        @PathVariable Long id,
                        @RequestBody Map<String, Object> request) {

                try {

                        Long roomId = Long.valueOf(
                                        request.get("roomId").toString());

                        String foodPlan =
                                        request.get("foodPlan").toString();

                        tenantService.makeTenantActive(
                                        id,
                                        roomId,
                                        foodPlan);

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "message",
                                                        "Tenant rejoined successfully."));

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(Map.of(
                                                        "message",
                                                        e.getMessage()));
                }
        }

        // ==================================================
        // GET MY TENANT DETAILS
        // ==================================================

        @GetMapping("/my")
        public ResponseEntity<?> getMyDetails(
                        Authentication authentication) {

                try {

                        String username =
                                        authentication.getName();

                        Tenant tenant =
                                        tenantService.getTenantByUsername(
                                                        username);

                        return ResponseEntity.ok(tenant);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(Map.of(
                                                        "message",
                                                        e.getMessage()));
                }
        }

        // ==================================================
        // UPDATE TENANT ROOM + FOOD PLAN
        // ==================================================

        @PutMapping("/{id}/room")
        public ResponseEntity<?> updateTenantRoom(
                        @PathVariable Long id,
                        @RequestBody Map<String, Object> request) {

                try {

                        Long roomId = Long.valueOf(
                                        request.get("roomId").toString());

                        String foodPlan =
                                        request.get("foodPlan").toString();

                        Tenant tenant =
                                        tenantService.updateTenantRoom(
                                                        id,
                                                        roomId,
                                                        foodPlan);

                        return ResponseEntity.ok(tenant);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(Map.of(
                                                        "message",
                                                        e.getMessage()));
                }
        }
}