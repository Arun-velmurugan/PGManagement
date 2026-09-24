package com.project.pghostel.app.service;

import com.project.pghostel.app.dto.TenantDashboardResponse;
import com.project.pghostel.app.entity.Complaint;
import com.project.pghostel.app.entity.Notice;
import com.project.pghostel.app.entity.Payment;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.repository.ComplaintRepository;
import com.project.pghostel.app.repository.NoticeRepository;
import com.project.pghostel.app.repository.PaymentRepository;
import com.project.pghostel.app.repository.TenantRepository;
import com.project.pghostel.app.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class TenantDashboardService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ComplaintRepository complaintRepository;
    private final NoticeRepository noticeRepository;


    public TenantDashboardService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
            ComplaintRepository complaintRepository,
            NoticeRepository noticeRepository) {

        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.complaintRepository = complaintRepository;
        this.noticeRepository = noticeRepository;
    }


    // ==================================================
    // GET TENANT DASHBOARD
    // ==================================================

    public TenantDashboardResponse getTenantDashboard(
            String username) {


        // ==================================================
        // FIND USER
        // ==================================================

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found."
                                )
                        );


        // ==================================================
        // CHECK ROLE
        // ==================================================

        if (!"TENANT".equalsIgnoreCase(
                user.getRole())) {

            throw new RuntimeException(
                    "Only tenant users can access this dashboard."
            );
        }


        // ==================================================
        // CHECK USER STATUS
        // ==================================================

        if (!"ACTIVE".equalsIgnoreCase(
                user.getStatus())) {

            throw new RuntimeException(
                    "Tenant account is inactive."
            );
        }


        // ==================================================
        // FIND TENANT
        // ==================================================

        Tenant tenant =
                tenantRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tenant details not found."
                                )
                        );


        // ==================================================
        // CHECK TENANT STATUS
        // ==================================================

        if (!"ACTIVE".equalsIgnoreCase(
                tenant.getStatus())) {

            throw new RuntimeException(
                    "Tenant is inactive."
            );
        }


        // ==================================================
        // CURRENT MONTH
        // ==================================================

        YearMonth currentMonth =
                YearMonth.now();


        String month =
                currentMonth.toString();


        String monthDisplay =
                currentMonth
                        .getMonth()
                        .getDisplayName(
                                TextStyle.FULL,
                                Locale.ENGLISH
                        )
                        + " "
                        + currentMonth.getYear();


        // ==================================================
        // RESPONSE
        // ==================================================

        TenantDashboardResponse response =
                new TenantDashboardResponse();


        // ==================================================
        // PROFILE
        // ==================================================

        response.setUsername(
                user.getUsername()
        );


        // ==================================================
        // CURRENT RENT
        // ==================================================

        response.setCurrentRent(
                tenant.getRent()
        );


        response.setCurrentRentPeriod(
                monthDisplay
        );


        // ==================================================
        // ROOM DETAILS
        // ==================================================

        if (tenant.getRoom() != null) {

            TenantDashboardResponse.RoomDetails room =
                    new TenantDashboardResponse.RoomDetails();


            room.setRoomNo(
                    tenant.getRoom().getRoomNo()
            );


            room.setRoomType(
                    tenant.getRoom().getRoomType()
            );


            room.setFood(
                    tenant.getFoodPlan()
            );


            room.setRent(
                    tenant.getRoom().getRent()
            );


            room.setStatus(
                    tenant.getRoom().getStatus()
            );


            response.setRoom(room);
        }


        // ==================================================
        // CURRENT MONTH PAYMENT
        // ==================================================

        List<Payment> payments =
                paymentRepository
                        .findByTenant_TenantId(
                                tenant.getTenantId()
                        );


        Payment currentPayment =
                payments
                        .stream()
                        .filter(payment ->
                                month.equals(
                                        payment.getMonth()
                                )
                        )
                        .findFirst()
                        .orElse(null);


        if (currentPayment != null) {

            TenantDashboardResponse.CurrentPayment payment =
                    new TenantDashboardResponse.CurrentPayment();


            payment.setRentPeriod(
                    monthDisplay
            );


            payment.setAmount(
                    currentPayment.getAmount()
            );


            payment.setDueDate(
                    currentPayment.getDueDate()
            );


            payment.setPaymentDate(
                    currentPayment.getPaymentDate()
            );


            payment.setStatus(
                    currentPayment.getStatus()
            );


            response.setCurrentPayment(
                    payment
            );


            response.setPaymentStatus(
                    currentPayment.getStatus()
            );

        } else {

            response.setCurrentPayment(null);

            response.setPaymentStatus("-");

        }


        response.setPaymentPeriod(
                monthDisplay
        );


        // ==================================================
        // COMPLAINTS
        // ==================================================

        List<Complaint> complaints =
                complaintRepository
                        .findByTenant_TenantIdOrderBySubmittedDateDesc(
                                tenant.getTenantId()
                        );


        // ==================================================
        // OPEN COMPLAINT COUNT
        // ==================================================

        long openComplaintsCount =
                complaints
                        .stream()
                        .filter(complaint ->
                                "OPEN".equalsIgnoreCase(
                                        complaint.getStatus()
                                )
                        )
                        .count();


        response.setOpenComplaintsCount(
                openComplaintsCount
        );


        // ==================================================
        // LATEST 3 COMPLAINTS
        // ==================================================

        List<TenantDashboardResponse.ComplaintDetails>
                latestComplaints =
                complaints
                        .stream()
                        .limit(3)
                        .map(complaint -> {

                            TenantDashboardResponse.ComplaintDetails details =
                                    new TenantDashboardResponse.ComplaintDetails();


                            details.setSubject(
                                    complaint.getTitle()
                            );


                            details.setDescription(
                                    complaint.getDescription()
                            );


                            details.setCreatedAt(
                                    complaint.getSubmittedDate()
                            );


                            details.setStatus(
                                    complaint.getStatus()
                            );


                            return details;

                        })
                        .collect(Collectors.toList());


        response.setLatestComplaints(
                latestComplaints
        );


        // ==================================================
        // LATEST NOTICES
        // ==================================================

        List<Notice> notices =
                noticeRepository
                        .findAllByOrderByPostedDateDesc();


        List<Notice> activeNotices =
                notices
                        .stream()
                        .filter(notice ->
                                !"CANCELLED".equalsIgnoreCase(
                                        notice.getStatus()
                                )
                        )
                        .limit(3)
                        .collect(Collectors.toList());


        // ==================================================
        // NOTICE RESPONSE
        // ==================================================

        List<TenantDashboardResponse.NoticeDetails>
                latestNotices =
                activeNotices
                        .stream()
                        .map(notice -> {

                            TenantDashboardResponse.NoticeDetails details =
                                    new TenantDashboardResponse.NoticeDetails();


                            details.setTitle(
                                    notice.getTitle()
                            );


                            details.setMessage(
                                    notice.getDescription()
                            );


                            details.setCreatedAt(
                                    notice.getPostedDate()
                            );


                            return details;

                        })
                        .collect(Collectors.toList());


        response.setLatestNotices(
                latestNotices
        );


        // ==================================================
        // RETURN
        // ==================================================

        return response;
    }
}