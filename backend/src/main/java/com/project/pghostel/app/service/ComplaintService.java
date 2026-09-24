package com.project.pghostel.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.pghostel.app.dto.ComplaintRequest;
import com.project.pghostel.app.dto.ComplaintResponse;
import com.project.pghostel.app.entity.Complaint;
import com.project.pghostel.app.entity.ComplaintNotification;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.repository.ComplaintNotificationRepository;
import com.project.pghostel.app.repository.ComplaintRepository;
import com.project.pghostel.app.repository.TenantRepository;
import com.project.pghostel.app.repository.UserRepository;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintNotificationRepository complaintNotificationRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;


    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================

    public ComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintNotificationRepository complaintNotificationRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository) {

        this.complaintRepository = complaintRepository;
        this.complaintNotificationRepository =
                complaintNotificationRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }


    // ==========================================================
    // TENANT - RAISE COMPLAINT
    // ==========================================================

    public ComplaintResponse createComplaint(
            ComplaintRequest request,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found."));


        Tenant tenant = tenantRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tenant not found."));


        // ------------------------------------------------------
        // VALIDATE CATEGORY
        // ------------------------------------------------------

        if (request.getCategory() == null ||
                request.getCategory().trim().isEmpty()) {

            throw new RuntimeException(
                    "Complaint category is required.");
        }


        // ------------------------------------------------------
        // VALIDATE TITLE
        // ------------------------------------------------------

        if (request.getTitle() == null ||
                request.getTitle().trim().isEmpty()) {

            throw new RuntimeException(
                    "Complaint title is required.");
        }


        // ------------------------------------------------------
        // VALIDATE DESCRIPTION
        // ------------------------------------------------------

        if (request.getDescription() == null ||
                request.getDescription().trim().isEmpty()) {

            throw new RuntimeException(
                    "Complaint description is required.");
        }


        // ------------------------------------------------------
        // CREATE COMPLAINT
        // ------------------------------------------------------

        Complaint complaint = new Complaint();

        complaint.setTenant(tenant);

        complaint.setCategory(
                request.getCategory().trim());

        complaint.setTitle(
                request.getTitle().trim());

        complaint.setDescription(
                request.getDescription().trim());

        complaint.setSubmittedDate(
                LocalDate.now());

        complaint.setLastUpdated(
                LocalDate.now());

        complaint.setStatus("OPEN");


        Complaint savedComplaint =
                complaintRepository.save(complaint);


        return convertToResponse(savedComplaint);
    }


    // ==========================================================
    // ADMIN / WARDEN - GET ALL COMPLAINTS
    // ==========================================================

    public List<ComplaintResponse> getAllComplaints() {

        return complaintRepository
                .findAllByOrderBySubmittedDateDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    // ==========================================================
    // TENANT - GET MY COMPLAINTS
    // ==========================================================

    public List<ComplaintResponse> getMyComplaints(
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found."));


        Tenant tenant = tenantRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tenant not found."));


        return complaintRepository
                .findByTenant_TenantIdOrderBySubmittedDateDesc(
                        tenant.getTenantId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    // ==========================================================
    // GET SINGLE COMPLAINT
    // ADMIN / WARDEN
    // ==========================================================

    public ComplaintResponse getComplaint(
            Long complaintId) {

        Complaint complaint =
                complaintRepository.findById(complaintId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Complaint not found."));


        return convertToResponse(complaint);
    }


    // ==========================================================
    // ADMIN / WARDEN - UPDATE STATUS
    // ==========================================================

    public ComplaintResponse updateStatus(
            Long complaintId,
            String newStatus) {

        Complaint complaint =
                complaintRepository.findById(complaintId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Complaint not found."));


        String currentStatus =
                complaint.getStatus();


        // ------------------------------------------------------
        // CANCELLED COMPLAINT
        // ------------------------------------------------------

        if ("CANCELLED".equalsIgnoreCase(
                currentStatus)) {

            throw new RuntimeException(
                    "Cancelled complaint cannot be updated.");
        }


        // ------------------------------------------------------
        // ALREADY RESOLVED
        // ------------------------------------------------------

        if ("RESOLVED".equalsIgnoreCase(
                currentStatus)) {

            throw new RuntimeException(
                    "Resolved complaint cannot be updated.");
        }


        // ------------------------------------------------------
        // ALREADY WAITING FOR CONFIRMATION
        // ------------------------------------------------------

        if ("WAITING_FOR_CONFIRMATION"
                .equalsIgnoreCase(currentStatus)) {

            throw new RuntimeException(
                    "Complaint is already waiting for tenant confirmation.");
        }


        // ------------------------------------------------------
        // ADMIN / WARDEN → IN PROGRESS
        // ------------------------------------------------------

        if ("IN_PROGRESS".equalsIgnoreCase(
                newStatus)) {

            complaint.setStatus(
                    "IN_PROGRESS");

            complaint.setLastUpdated(
                    LocalDate.now());


            Complaint saved =
                    complaintRepository.save(complaint);


            return convertToResponse(saved);
        }


        // ------------------------------------------------------
        // ADMIN / WARDEN → RESOLVED
        //
        // DON'T RESOLVE DIRECTLY.
        //
        // Change status to WAITING_FOR_CONFIRMATION
        // and create notification for tenant.
        // ------------------------------------------------------

        if ("RESOLVED".equalsIgnoreCase(
                newStatus)) {

            complaint.setStatus(
                    "WAITING_FOR_CONFIRMATION");

            complaint.setLastUpdated(
                    LocalDate.now());


            Complaint saved =
                    complaintRepository.save(complaint);


            // --------------------------------------------------
            // CREATE NOTIFICATION
            // --------------------------------------------------

            ComplaintNotification notification =
                    new ComplaintNotification();


            notification.setComplaint(saved);

            notification.setTenant(
                    saved.getTenant());


            notification.setTitle(
                    "Complaint Resolution Confirmation");


            notification.setMessage(
                    "Your complaint \"" +
                    saved.getTitle() +
                    "\" has been marked as resolved. " +
                    "Please confirm whether the issue has been resolved.");


            notification.setRead(false);

            notification.setCreatedAt(
                    LocalDateTime.now());


            complaintNotificationRepository.save(
                    notification);


            return convertToResponse(saved);
        }


        // ------------------------------------------------------
        // ADMIN / WARDEN → OPEN
        // ------------------------------------------------------

        if ("OPEN".equalsIgnoreCase(
                newStatus)) {

            complaint.setStatus("OPEN");

            complaint.setLastUpdated(
                    LocalDate.now());


            Complaint saved =
                    complaintRepository.save(complaint);


            return convertToResponse(saved);
        }


        // ------------------------------------------------------
        // INVALID STATUS
        // ------------------------------------------------------

        throw new RuntimeException(
                "Invalid complaint status.");
    }


    // ==========================================================
    // TENANT - CONFIRM RESOLUTION
    // ==========================================================

    public ComplaintResponse confirmResolution(
            Long complaintId,
            String username) {

        Complaint complaint =
                getTenantComplaint(
                        complaintId,
                        username);


        // ------------------------------------------------------
        // CHECK WAITING STATUS
        // ------------------------------------------------------

        if (!"WAITING_FOR_CONFIRMATION"
                .equalsIgnoreCase(
                        complaint.getStatus())) {

            throw new RuntimeException(
                    "This complaint is not waiting for confirmation.");
        }


        // ------------------------------------------------------
        // CHANGE TO RESOLVED
        // ------------------------------------------------------

        complaint.setStatus("RESOLVED");

        complaint.setLastUpdated(
                LocalDate.now());


        Complaint saved =
                complaintRepository.save(complaint);


        // ------------------------------------------------------
        // MARK NOTIFICATION AS READ
        // ------------------------------------------------------

        ComplaintNotification notification =
                complaintNotificationRepository
                        .findByComplaintComplaintIdAndTenantTenantId(
                                complaintId,
                                complaint.getTenant()
                                        .getTenantId())
                        .orElse(null);


        if (notification != null) {

            notification.setRead(true);

            complaintNotificationRepository.save(
                    notification);
        }


        return convertToResponse(saved);
    }


    // ==========================================================
    // TENANT - REJECT RESOLUTION
    // ==========================================================

    public ComplaintResponse rejectResolution(
            Long complaintId,
            String username) {

        Complaint complaint =
                getTenantComplaint(
                        complaintId,
                        username);


        // ------------------------------------------------------
        // CHECK WAITING STATUS
        // ------------------------------------------------------

        if (!"WAITING_FOR_CONFIRMATION"
                .equalsIgnoreCase(
                        complaint.getStatus())) {

            throw new RuntimeException(
                    "This complaint is not waiting for confirmation.");
        }


        // ------------------------------------------------------
        // RETURN TO OPEN
        // ------------------------------------------------------

        complaint.setStatus("OPEN");

        complaint.setLastUpdated(
                LocalDate.now());


        Complaint saved =
                complaintRepository.save(complaint);


        // ------------------------------------------------------
        // MARK NOTIFICATION AS READ
        // ------------------------------------------------------

        ComplaintNotification notification =
                complaintNotificationRepository
                        .findByComplaintComplaintIdAndTenantTenantId(
                                complaintId,
                                complaint.getTenant()
                                        .getTenantId())
                        .orElse(null);


        if (notification != null) {

            notification.setRead(true);

            complaintNotificationRepository.save(
                    notification);
        }


        return convertToResponse(saved);
    }


    // ==========================================================
    // TENANT - CANCEL COMPLAINT
    // ==========================================================

    public ComplaintResponse cancelComplaint(
            Long complaintId,
            String username) {

        Complaint complaint =
                getTenantComplaint(
                        complaintId,
                        username);


        // ------------------------------------------------------
        // ONLY OPEN COMPLAINT CAN BE CANCELLED
        // ------------------------------------------------------

        if (!"OPEN".equalsIgnoreCase(
                complaint.getStatus())) {

            throw new RuntimeException(
                    "Only open complaints can be cancelled.");
        }


        // ------------------------------------------------------
        // CHANGE STATUS
        // ------------------------------------------------------

        complaint.setStatus("CANCELLED");

        complaint.setLastUpdated(
                LocalDate.now());


        Complaint saved =
                complaintRepository.save(complaint);


        return convertToResponse(saved);
    }


    // ==========================================================
    // VERIFY COMPLAINT BELONGS TO TENANT
    // ==========================================================

    private Complaint getTenantComplaint(
            Long complaintId,
            String username) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."));


        Tenant tenant =
                tenantRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant not found."));


        Complaint complaint =
                complaintRepository.findById(complaintId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Complaint not found."));


        // ------------------------------------------------------
        // CHECK TENANT OWNERSHIP
        // ------------------------------------------------------

        if (!complaint.getTenant()
                .getTenantId()
                .equals(tenant.getTenantId())) {

            throw new RuntimeException(
                    "You cannot access this complaint.");
        }


        return complaint;
    }


    // ==========================================================
    // CONVERT ENTITY -> RESPONSE
    // ==========================================================

    private ComplaintResponse convertToResponse(
            Complaint complaint) {

        ComplaintResponse response =
                new ComplaintResponse();


        Tenant tenant =
                complaint.getTenant();


        // ------------------------------------------------------
        // COMPLAINT ID
        // ------------------------------------------------------

        response.setComplaintId(
                complaint.getComplaintId());


        // ------------------------------------------------------
        // TENANT ID
        // ------------------------------------------------------

        response.setTenantId(
                tenant.getTenantId());


        // ------------------------------------------------------
        // TENANT DETAILS
        // ------------------------------------------------------

        if (tenant.getUser() != null) {

            response.setTenantName(
                    tenant.getUser().getFullName());

            response.setPhone(
                    tenant.getUser().getPhone());
        }


        // ------------------------------------------------------
        // ROOM DETAILS
        // ------------------------------------------------------

        if (tenant.getRoom() != null) {

            response.setRoomNo(
                    tenant.getRoom().getRoomNo());
        }


        // ------------------------------------------------------
        // COMPLAINT DETAILS
        // ------------------------------------------------------

        response.setCategory(
                complaint.getCategory());

        response.setTitle(
                complaint.getTitle());

        response.setDescription(
                complaint.getDescription());


        // ------------------------------------------------------
        // DATES
        // ------------------------------------------------------

        response.setSubmittedDate(
                complaint.getSubmittedDate());

        response.setLastUpdated(
                complaint.getLastUpdated());


        // ------------------------------------------------------
        // STATUS
        // ------------------------------------------------------

        response.setStatus(
                complaint.getStatus());


        // ------------------------------------------------------
        // PENDING CONFIRMATION
        // ------------------------------------------------------

        response.setPendingConfirmation(
                "WAITING_FOR_CONFIRMATION"
                        .equalsIgnoreCase(
                                complaint.getStatus()));


        return response;
    }
}