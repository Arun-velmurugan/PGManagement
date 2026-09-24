package com.project.pghostel.app.controller;

import com.project.pghostel.app.dto.LeaveResponse;
import com.project.pghostel.app.service.LeaveService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(
            LeaveService leaveService) {

        this.leaveService = leaveService;
    }


    // ==================================================
    // TENANT - ADD
    // ==================================================

    @PostMapping("/my")
    public LeaveResponse addLeave(
            Authentication authentication,
            @RequestBody LeaveRequest request) {

        return leaveService.addLeave(
                authentication.getName(),
                request.getFromDate(),
                request.getReturnDate(),
                request.getReason()
        );
    }


    // ==================================================
    // TENANT - ACTIVE LEAVES
    // ==================================================

    @GetMapping("/my")
    public List<LeaveResponse> getMyLeaves(
            Authentication authentication) {

        return leaveService.getMyActiveLeaves(
                authentication.getName()
        );
    }


    // ==================================================
    // TENANT - HISTORY
    // ==================================================

    @GetMapping("/my/history")
    public List<LeaveResponse> getMyHistory(
            Authentication authentication) {

        return leaveService.getMyHistory(
                authentication.getName()
        );
    }


    // ==================================================
    // TENANT - EDIT
    // ==================================================

    @PutMapping("/my/{leaveId}")
    public LeaveResponse updateLeave(
            Authentication authentication,
            @PathVariable Long leaveId,
            @RequestBody LeaveRequest request) {

        return leaveService.updateLeave(
                authentication.getName(),
                leaveId,
                request.getFromDate(),
                request.getReturnDate(),
                request.getReason()
        );
    }


    // ==================================================
    // TENANT - CANCEL
    // ==================================================

    @PutMapping("/my/{leaveId}/cancel")
    public LeaveResponse cancelLeave(
            Authentication authentication,
            @PathVariable Long leaveId) {

        return leaveService.cancelLeave(
                authentication.getName(),
                leaveId
        );
    }


    // ==================================================
    // ADMIN / WARDEN
    // ==================================================

    @GetMapping
    public List<LeaveResponse> getAllLeaves() {

        return leaveService.getAllLeaves();
    }


    // ==================================================
    // REQUEST DTO
    // ==================================================

    public static class LeaveRequest {

        private LocalDate fromDate;
        private LocalDate returnDate;
        private String reason;


        public LocalDate getFromDate() {
            return fromDate;
        }

        public void setFromDate(
                LocalDate fromDate) {

            this.fromDate = fromDate;
        }


        public LocalDate getReturnDate() {
            return returnDate;
        }

        public void setReturnDate(
                LocalDate returnDate) {

            this.returnDate = returnDate;
        }


        public String getReason() {
            return reason;
        }

        public void setReason(
                String reason) {

            this.reason = reason;
        }
    }
}