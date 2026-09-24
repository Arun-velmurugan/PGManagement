package com.project.pghostel.app.service;

import com.project.pghostel.app.dto.LeaveResponse;
import com.project.pghostel.app.entity.Leave;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.repository.LeaveRepository;
import com.project.pghostel.app.repository.TenantRepository;
import com.project.pghostel.app.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public LeaveService(
            LeaveRepository leaveRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository) {

        this.leaveRepository = leaveRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    // ==================================================
    // ADD LEAVE
    // ==================================================

    public LeaveResponse addLeave(
            String username,
            LocalDate fromDate,
            LocalDate returnDate,
            String reason) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "User not found."));

        Tenant tenant = tenantRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Tenant details not found."));

        if (!"ACTIVE".equalsIgnoreCase(
                tenant.getStatus())) {

            throw new RuntimeException(
                    "Tenant is not active.");
        }

        if (fromDate == null ||
                returnDate == null) {

            throw new RuntimeException(
                    "Leave date and return date are required.");
        }

        if (returnDate.isBefore(fromDate)) {

            throw new RuntimeException(
                    "Return date cannot be before leave date.");
        }

        if (reason == null ||
                reason.trim().isEmpty()) {

            throw new RuntimeException(
                    "Reason is required.");
        }

        // Check overlapping leave
        List<Leave> existingLeaves = leaveRepository
                .findByTenant_TenantIdOrderByFromDateAsc(
                        tenant.getTenantId());

        for (Leave leave : existingLeaves) {

            if ("COMPLETED".equalsIgnoreCase(
                    leave.getStatus())
                    ||
                    "CANCELLED".equalsIgnoreCase(
                            leave.getStatus())) {

                continue;
            }

            boolean overlap = !returnDate.isBefore(
                    leave.getFromDate())
                    &&
                    !fromDate.isAfter(
                            leave.getReturnDate());

            if (overlap) {

                throw new RuntimeException(
                        "You already have a leave during this period.");
            }
        }

        Leave leave = new Leave();

        leave.setTenant(tenant);
        leave.setFromDate(fromDate);
        leave.setReturnDate(returnDate);
        leave.setReason(reason);

        updateStatus(leave);

        leave = leaveRepository.save(leave);

        return convertToResponse(leave);
    }

    // ==================================================
    // GET MY ACTIVE LEAVES
    // ==================================================

    public List<LeaveResponse> getMyActiveLeaves(
            String username) {

        Tenant tenant = getTenant(username);

        List<Leave> leaves = leaveRepository
                .findByTenant_TenantIdOrderByFromDateAsc(
                        tenant.getTenantId());

        List<LeaveResponse> result = new ArrayList<>();

        for (Leave leave : leaves) {

            updateStatus(leave);

            if ("UPCOMING".equalsIgnoreCase(
                    leave.getStatus())
                    ||
                    "ON LEAVE".equalsIgnoreCase(
                            leave.getStatus())) {

                result.add(
                        convertToResponse(leave));
            }
        }

        return result;
    }

    // ==================================================
    // GET MY HISTORY
    // ==================================================

    public List<LeaveResponse> getMyHistory(
            String username) {

        Tenant tenant = getTenant(username);

        List<Leave> leaves = leaveRepository
                .findByTenant_TenantIdOrderByFromDateAsc(
                        tenant.getTenantId());

        List<LeaveResponse> result = new ArrayList<>();

        for (Leave leave : leaves) {

            updateStatus(leave);

            if ("COMPLETED".equalsIgnoreCase(
                    leave.getStatus())
                    ||
                    "CANCELLED".equalsIgnoreCase(
                            leave.getStatus())) {

                result.add(
                        convertToResponse(leave));
            }
        }

        return result;
    }

    // ==================================================
    // UPDATE LEAVE
    // ==================================================

    public LeaveResponse updateLeave(
            String username,
            Long leaveId,
            LocalDate fromDate,
            LocalDate returnDate,
            String reason) {

        Tenant tenant = getTenant(username);

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException(
                        "Leave not found."));

        if (!leave.getTenant()
                .getTenantId()
                .equals(tenant.getTenantId())) {

            throw new RuntimeException(
                    "You cannot edit this leave.");
        }

        updateStatus(leave);

        if ("COMPLETED".equalsIgnoreCase(
                leave.getStatus())
                ||
                "CANCELLED".equalsIgnoreCase(
                        leave.getStatus())) {

            throw new RuntimeException(
                    "This leave cannot be edited.");
        }

        if (returnDate.isBefore(fromDate)) {

            throw new RuntimeException(
                    "Return date cannot be before leave date.");
        }

        leave.setFromDate(fromDate);
        leave.setReturnDate(returnDate);
        leave.setReason(reason);

        updateStatus(leave);

        leave = leaveRepository.save(leave);

        return convertToResponse(leave);
    }

    // ==================================================
    // CANCEL LEAVE
    // ==================================================

    public LeaveResponse cancelLeave(
            String username,
            Long leaveId) {

        Tenant tenant = getTenant(username);

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException(
                        "Leave not found."));

        if (!leave.getTenant()
                .getTenantId()
                .equals(tenant.getTenantId())) {

            throw new RuntimeException(
                    "You cannot cancel this leave.");
        }

        updateStatus(leave);

        if ("COMPLETED".equalsIgnoreCase(
                leave.getStatus())) {

            throw new RuntimeException(
                    "Completed leave cannot be cancelled.");
        }

        leave.setStatus("CANCELLED");

        leave = leaveRepository.save(leave);

        return convertToResponse(leave);
    }

    // ==================================================
    // ADMIN / WARDEN - ALL LEAVES
    // ==================================================

    public List<LeaveResponse> getAllLeaves() {

        List<Leave> leaves = leaveRepository
                .findAllByOrderByFromDateAsc();

        List<LeaveResponse> result = new ArrayList<>();

        for (Leave leave : leaves) {

            updateStatus(leave);

            result.add(
                    convertToResponse(leave));
        }

        return result;
    }

    // ==================================================
    // STATUS
    // ==================================================

    private void updateStatus(Leave leave) {

        if ("CANCELLED".equalsIgnoreCase(
                leave.getStatus())) {

            return;
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(
                leave.getFromDate())) {

            leave.setStatus("UPCOMING");

        } else if (today.isBefore(
                leave.getReturnDate())) {

            leave.setStatus("ON LEAVE");

        } else {

            leave.setStatus("COMPLETED");
        }

        leaveRepository.save(leave);
    }

    // ==================================================
    // GET TENANT
    // ==================================================

    private Tenant getTenant(
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "User not found."));

        return tenantRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Tenant details not found."));
    }

    // ==================================================
    // CONVERT ENTITY -> DTO
    // ==================================================

    private LeaveResponse convertToResponse(
            Leave leave) {

        Tenant tenant = leave.getTenant();

        LeaveResponse response = new LeaveResponse();

        response.setLeaveId(
                leave.getLeaveId());

        response.setTenantId(
                tenant.getTenantId());

        response.setTenantName(
                tenant.getUser().getFullName());

        response.setRoomNo(
                tenant.getRoom().getRoomNo());

        response.setPhoneNumber(
                tenant.getUser().getPhone());

        response.setFromDate(
                leave.getFromDate());

        response.setReturnDate(
                leave.getReturnDate());

        response.setReason(
                leave.getReason());

        response.setStatus(
                leave.getStatus());

        return response;
    }
}