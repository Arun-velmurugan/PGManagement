package com.project.pghostel.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.pghostel.app.entity.ComplaintNotification;

public interface ComplaintNotificationRepository
        extends JpaRepository<ComplaintNotification, Long> {


    // ==========================================================
    // GET ALL NOTIFICATIONS FOR A TENANT
    // ==========================================================

    List<ComplaintNotification>
    findByTenantTenantIdOrderByCreatedAtDesc(
            Long tenantId);


    // ==========================================================
    // COUNT UNREAD NOTIFICATIONS
    // ==========================================================

    long countByTenantTenantIdAndReadFalse(
            Long tenantId);


    // ==========================================================
    // GET NOTIFICATION FOR A SPECIFIC COMPLAINT
    // ==========================================================

    Optional<ComplaintNotification>
    findByComplaintComplaintIdAndTenantTenantId(
            Long complaintId,
            Long tenantId);


    // ==========================================================
    // GET ALL NOTIFICATIONS FOR A SPECIFIC COMPLAINT
    // ==========================================================

    List<ComplaintNotification>
    findByComplaintComplaintIdOrderByCreatedAtDesc(
            Long complaintId);
}