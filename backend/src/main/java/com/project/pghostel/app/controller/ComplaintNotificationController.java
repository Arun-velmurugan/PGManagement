package com.project.pghostel.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import com.project.pghostel.app.dto.ComplaintNotificationResponse;
import com.project.pghostel.app.entity.ComplaintNotification;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.repository.ComplaintNotificationRepository;
import com.project.pghostel.app.repository.TenantRepository;
import com.project.pghostel.app.repository.UserRepository;


@RestController
@RequestMapping("/api/complaint-notifications")
@CrossOrigin
public class ComplaintNotificationController {


    private final ComplaintNotificationRepository complaintNotificationRepository;

    private final TenantRepository tenantRepository;

    private final UserRepository userRepository;


    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================

    public ComplaintNotificationController(
            ComplaintNotificationRepository complaintNotificationRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository) {

        this.complaintNotificationRepository =
                complaintNotificationRepository;

        this.tenantRepository =
                tenantRepository;

        this.userRepository =
                userRepository;
    }


    // ==========================================================
    // TENANT - GET ALL MY NOTIFICATIONS
    // ==========================================================

    @GetMapping("/my")
    public ResponseEntity<?> getMyNotifications(
            Authentication authentication) {

        try {

            checkTenant(authentication);


            Tenant tenant =
                    getTenant(authentication.getName());


            List<ComplaintNotification> notifications =
                    complaintNotificationRepository
                            .findByTenantTenantIdOrderByCreatedAtDesc(
                                    tenant.getTenantId());


            List<ComplaintNotificationResponse> response =
                    notifications.stream()
                            .map(this::convertToResponse)
                            .toList();


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
    // TENANT - GET UNREAD NOTIFICATION COUNT
    // ==========================================================

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(
            Authentication authentication) {

        try {

            checkTenant(authentication);


            Tenant tenant =
                    getTenant(authentication.getName());


            long count =
                    complaintNotificationRepository
                            .countByTenantTenantIdAndReadFalse(
                                    tenant.getTenantId());


            return ResponseEntity.ok(count);

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
    // TENANT - MARK NOTIFICATION AS READ
    // ==========================================================

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        try {

            checkTenant(authentication);


            Tenant tenant =
                    getTenant(authentication.getName());


            ComplaintNotification notification =
                    complaintNotificationRepository
                            .findById(notificationId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Notification not found."));


            // --------------------------------------------------
            // SECURITY CHECK
            // --------------------------------------------------

            if (!notification.getTenant()
                    .getTenantId()
                    .equals(tenant.getTenantId())) {

                throw new RuntimeException(
                        "You cannot access this notification.");
            }


            notification.setRead(true);


            complaintNotificationRepository
                    .save(notification);


            ComplaintNotificationResponse response =
                    convertToResponse(notification);


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
    // CONVERT ENTITY → DTO
    // ==========================================================

    private ComplaintNotificationResponse convertToResponse(
            ComplaintNotification notification) {


        ComplaintNotificationResponse response =
                new ComplaintNotificationResponse();


        response.setNotificationId(
                notification.getNotificationId());


        response.setComplaintId(
                notification.getComplaint()
                        .getComplaintId());


        response.setTitle(
                notification.getTitle());


        response.setMessage(
                notification.getMessage());


        response.setRead(
                notification.isRead());


        response.setCreatedAt(
                notification.getCreatedAt());


        return response;
    }


    // ==========================================================
    // GET TENANT FROM USERNAME
    // ==========================================================

    private Tenant getTenant(String username) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."));


        return tenantRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tenant not found."));
    }


    // ==========================================================
    // CHECK TENANT ROLE
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
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(authority ->
                                "ROLE_TENANT".equals(
                                        authority));


        if (!allowed) {

            throw new RuntimeException(
                    "Only tenants can access notifications.");
        }
    }
}