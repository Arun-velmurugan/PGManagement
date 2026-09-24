package com.project.pghostel.app.controller;

import com.project.pghostel.app.dto.TenantDashboardResponse;
import com.project.pghostel.app.service.TenantDashboardService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant-dashboard")
@CrossOrigin
public class TenantDashboardController {

    private final TenantDashboardService tenantDashboardService;


    public TenantDashboardController(
            TenantDashboardService tenantDashboardService) {

        this.tenantDashboardService =
                tenantDashboardService;
    }


    // ==================================================
    // GET LOGGED-IN TENANT DASHBOARD
    // ==================================================

    @GetMapping
    public ResponseEntity<?> getTenantDashboard(
            Authentication authentication) {

        try {

            // ==================================================
            // CHECK LOGIN
            // ==================================================

            if (authentication == null ||
                    !authentication.isAuthenticated()) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Please login.");
            }


            // ==================================================
            // GET USERNAME FROM JWT
            // ==================================================

            String username =
                    authentication.getName();


            // ==================================================
            // GET DASHBOARD
            // ==================================================

            TenantDashboardResponse dashboard =
                    tenantDashboardService
                            .getTenantDashboard(username);


            return ResponseEntity.ok(dashboard);


        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());


        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}