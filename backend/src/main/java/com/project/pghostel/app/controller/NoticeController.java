package com.project.pghostel.app.controller;

import com.project.pghostel.app.dto.FeedbackRequest;
import com.project.pghostel.app.dto.NoticeRequest;
import com.project.pghostel.app.dto.NoticeResponse;
import com.project.pghostel.app.entity.NoticeFeedback;
import com.project.pghostel.app.service.NoticeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@CrossOrigin
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }


    // ==================================================
    // ADMIN / WARDEN - ADD NOTICE
    // ==================================================

    @PostMapping
    public ResponseEntity<?> addNotice(
            @RequestBody NoticeRequest request,
            Authentication authentication) {

        try {

            checkAdminOrWarden(authentication);

            NoticeResponse response =
                    noticeService.addNotice(
                            request,
                            authentication.getName()
                    );

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


    // ==================================================
    // ADMIN / WARDEN - GET ALL NOTICES
    // ==================================================

    @GetMapping
    public ResponseEntity<?> getAllNotices(
            Authentication authentication) {

        try {

            checkAdminOrWarden(authentication);

            return ResponseEntity.ok(
                    noticeService.getAllNotices()
            );

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


    // ==================================================
    // TENANT - GET MY NOTICES
    // ==================================================

    @GetMapping("/my")
    public ResponseEntity<?> getMyNotices(
            Authentication authentication) {

        try {

            checkTenant(authentication);

            return ResponseEntity.ok(
                    noticeService.getTenantNotices(
                            authentication.getName()
                    )
            );

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


    // ==================================================
    // VIEW SINGLE NOTICE
    // ==================================================

    @GetMapping("/{noticeId}")
    public ResponseEntity<?> getNotice(
            @PathVariable Long noticeId,
            Authentication authentication) {

        try {

            if (authentication == null ||
                    !authentication.isAuthenticated()) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Please login.");
            }

            return ResponseEntity.ok(
                    noticeService.getNotice(noticeId)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // ==================================================
    // TENANT - SUBMIT FEEDBACK
    // ==================================================

    @PostMapping("/{noticeId}/feedback")
    public ResponseEntity<?> submitFeedback(
            @PathVariable Long noticeId,
            @RequestBody FeedbackRequest request,
            Authentication authentication) {

        try {

            checkTenant(authentication);

            String message =
                    noticeService.submitFeedback(
                            authentication.getName(),
                            noticeId,
                            request
                    );

            return ResponseEntity.ok(message);

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


    // ==================================================
    // ADMIN / WARDEN - VIEW FEEDBACK
    // ==================================================

    @GetMapping("/{noticeId}/feedback")
    public ResponseEntity<?> getFeedback(
            @PathVariable Long noticeId,
            Authentication authentication) {

        try {

            checkAdminOrWarden(authentication);

            List<NoticeFeedback> feedback =
                    noticeService.getNoticeFeedback(noticeId);

            return ResponseEntity.ok(feedback);

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


    // ==================================================
    // ADMIN / WARDEN - CANCEL NOTICE
    // ==================================================

    @PutMapping("/{noticeId}/cancel")
    public ResponseEntity<?> cancelNotice(
            @PathVariable Long noticeId,
            Authentication authentication) {

        try {

            checkAdminOrWarden(authentication);

            return ResponseEntity.ok(
                    noticeService.cancelNotice(noticeId)
            );

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


    // ==================================================
    // ROLE CHECK - ADMIN / WARDEN
    // ==================================================

    private void checkAdminOrWarden(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "Please login."
            );
        }

        boolean allowed =
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(
                                role ->
                                        "ROLE_ADMIN".equals(role)
                                        ||
                                        "ROLE_WARDEN".equals(role)
                        );

        if (!allowed) {

            throw new RuntimeException(
                    "Access denied."
            );
        }
    }


    // ==================================================
    // ROLE CHECK - TENANT
    // ==================================================

    private void checkTenant(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "Please login."
            );
        }

        boolean allowed =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        "ROLE_TENANT".equals(
                                                authority.getAuthority()
                                        )
                        );

        if (!allowed) {

            throw new RuntimeException(
                    "Only tenants can access this feature."
            );
        }
    }
}