package com.project.pghostel.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import com.project.pghostel.app.dto.ComplaintRequest;
import com.project.pghostel.app.dto.ComplaintResponse;
import com.project.pghostel.app.service.ComplaintService;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }


    // ==========================================================
    // TENANT - RAISE COMPLAINT
    // ==========================================================

    @PostMapping
    public ResponseEntity<?> createComplaint(
            @RequestBody ComplaintRequest request,
            Authentication authentication) {

        try {

            checkTenant(authentication);

            ComplaintResponse response =
                    complaintService.createComplaint(
                            request,
                            authentication.getName());

            return ResponseEntity.ok(response);

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


    // ==========================================================
    // ADMIN / WARDEN - GET ALL COMPLAINTS
    // ==========================================================

    @GetMapping
    public ResponseEntity<?> getAllComplaints(
            Authentication authentication) {

        try {

            checkAdminOrWarden(authentication);

            List<ComplaintResponse> complaints =
                    complaintService.getAllComplaints();

            return ResponseEntity.ok(complaints);

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


    // ==========================================================
    // TENANT - GET MY COMPLAINTS
    // ==========================================================

    @GetMapping("/my")
    public ResponseEntity<?> getMyComplaints(
            Authentication authentication) {

        try {

            checkTenant(authentication);

            List<ComplaintResponse> complaints =
                    complaintService.getMyComplaints(
                            authentication.getName());

            return ResponseEntity.ok(complaints);

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


    // ==========================================================
    // GET SINGLE COMPLAINT
    // ADMIN / WARDEN
    // ==========================================================

    @GetMapping("/{complaintId}")
    public ResponseEntity<?> getComplaint(
            @PathVariable Long complaintId,
            Authentication authentication) {

        try {

            checkAdminOrWarden(authentication);

            return ResponseEntity.ok(
                    complaintService.getComplaint(
                            complaintId));

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


    // ==========================================================
    // ADMIN / WARDEN - UPDATE STATUS
    // ==========================================================

    @PutMapping("/{complaintId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long complaintId,
            @RequestParam String status,
            Authentication authentication) {

        try {

            checkAdminOrWarden(authentication);

            ComplaintResponse response =
                    complaintService.updateStatus(
                            complaintId,
                            status);

            return ResponseEntity.ok(response);

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


    // ==========================================================
    // TENANT - CONFIRM RESOLUTION
    // ==========================================================

    @PutMapping("/{complaintId}/confirm")
    public ResponseEntity<?> confirmResolution(
            @PathVariable Long complaintId,
            Authentication authentication) {

        try {

            checkTenant(authentication);

            ComplaintResponse response =
                    complaintService.confirmResolution(
                            complaintId,
                            authentication.getName());

            return ResponseEntity.ok(response);

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


    // ==========================================================
    // TENANT - REJECT RESOLUTION
    // ==========================================================

    @PutMapping("/{complaintId}/reject")
    public ResponseEntity<?> rejectResolution(
            @PathVariable Long complaintId,
            Authentication authentication) {

        try {

            checkTenant(authentication);

            ComplaintResponse response =
                    complaintService.rejectResolution(
                            complaintId,
                            authentication.getName());

            return ResponseEntity.ok(response);

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


    // ==========================================================
    // TENANT - CANCEL COMPLAINT
    // ==========================================================

    @PutMapping("/{complaintId}/cancel")
    public ResponseEntity<?> cancelComplaint(
            @PathVariable Long complaintId,
            Authentication authentication) {

        try {

            checkTenant(authentication);

            ComplaintResponse response =
                    complaintService.cancelComplaint(
                            complaintId,
                            authentication.getName());

            return ResponseEntity.ok(response);

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


    // ==========================================================
    // CHECK ADMIN / WARDEN
    // ==========================================================

    private void checkAdminOrWarden(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "Please login.");
        }

        boolean allowed =
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(role ->
                                "ROLE_ADMIN".equals(role) ||
                                "ROLE_WARDEN".equals(role));

        if (!allowed) {

            throw new RuntimeException(
                    "Access denied.");
        }
    }


    // ==========================================================
    // CHECK TENANT
    // ==========================================================

    private void checkTenant(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "Please login.");
        }

        boolean allowed =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_TENANT".equals(
                                        authority.getAuthority()));

        if (!allowed) {

            throw new RuntimeException(
                    "Only tenants can access this feature.");
        }
    }
}