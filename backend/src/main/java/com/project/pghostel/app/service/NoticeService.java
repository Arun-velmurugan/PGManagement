package com.project.pghostel.app.service;

import com.project.pghostel.app.dto.FeedbackRequest;
import com.project.pghostel.app.dto.NoticeRequest;
import com.project.pghostel.app.dto.NoticeResponse;
import com.project.pghostel.app.entity.Notice;
import com.project.pghostel.app.entity.NoticeFeedback;
import com.project.pghostel.app.entity.Tenant;
import com.project.pghostel.app.entity.User;
import com.project.pghostel.app.repository.NoticeFeedbackRepository;
import com.project.pghostel.app.repository.NoticeRepository;
import com.project.pghostel.app.repository.TenantRepository;
import com.project.pghostel.app.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeFeedbackRepository feedbackRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;


    public NoticeService(
            NoticeRepository noticeRepository,
            NoticeFeedbackRepository feedbackRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository) {

        this.noticeRepository = noticeRepository;
        this.feedbackRepository = feedbackRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }


    // ==================================================
    // ADD NOTICE
    // ==================================================

    public NoticeResponse addNotice(
            NoticeRequest request,
            String username) {

        validateRequest(request);


        Notice notice = new Notice();

        notice.setNoticeType(
                request.getNoticeType()
        );

        notice.setTitle(
                request.getTitle()
        );

        notice.setDescription(
                request.getDescription()
        );

        notice.setPostedDate(
                LocalDate.now()
        );

        notice.setEffectiveDate(
                request.getEffectiveDate()
        );

        notice.setFromTime(
                request.getFromTime()
        );

        notice.setToTime(
                request.getToTime()
        );

        notice.setFeedbackRequired(
                request.isFeedbackRequired()
        );

        notice.setCreatedBy(
                username
        );


        updateStatus(notice);


        notice =
                noticeRepository.save(notice);


        return convertToResponse(
                notice,
                false
        );
    }


    // ==================================================
    // GET ALL NOTICES - ADMIN / WARDEN
    // ==================================================

    public List<NoticeResponse> getAllNotices() {

        List<Notice> notices =
                noticeRepository
                        .findAllByOrderByPostedDateDesc();


        List<NoticeResponse> result =
                new ArrayList<>();


        for (Notice notice : notices) {

            updateStatus(notice);

            result.add(
                    convertToResponse(
                            notice,
                            false
                    )
            );
        }


        return result;
    }


    // ==================================================
    // GET TENANT NOTICES
    // ==================================================

    public List<NoticeResponse> getTenantNotices(
            String username) {

        Tenant tenant =
                getTenant(username);


        List<Notice> notices =
                noticeRepository
                        .findAllByOrderByPostedDateDesc();


        List<NoticeResponse> result =
                new ArrayList<>();


        for (Notice notice : notices) {

            updateStatus(notice);


            // Cancelled notices are not shown to tenant

            if ("CANCELLED".equalsIgnoreCase(
                    notice.getStatus())) {

                continue;
            }


            // Future notices can be shown as Upcoming

            boolean feedbackSubmitted =
                    feedbackRepository
                            .existsByNotice_NoticeIdAndTenant_TenantId(
                                    notice.getNoticeId(),
                                    tenant.getTenantId()
                            );


            result.add(
                    convertToResponse(
                            notice,
                            feedbackSubmitted
                    )
            );
        }


        return result;
    }


    // ==================================================
    // GET SINGLE NOTICE
    // ==================================================

    public NoticeResponse getNotice(
            Long noticeId) {

        Notice notice =
                noticeRepository.findById(noticeId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Notice not found."
                                )
                        );


        updateStatus(notice);


        return convertToResponse(
                notice,
                false
        );
    }


    // ==================================================
    // SUBMIT FEEDBACK
    // ==================================================

    public String submitFeedback(
            String username,
            Long noticeId,
            FeedbackRequest request) {


        if (request == null ||
                request.getFeedback() == null ||
                request.getFeedback().trim().isEmpty()) {

            throw new RuntimeException(
                    "Feedback is required."
            );
        }


        Tenant tenant =
                getTenant(username);


        Notice notice =
                noticeRepository.findById(noticeId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Notice not found."
                                )
                        );


        if (!notice.isFeedbackRequired()) {

            throw new RuntimeException(
                    "Feedback is not required for this notice."
            );
        }


        updateStatus(notice);


        if ("CANCELLED".equalsIgnoreCase(
                notice.getStatus())) {

            throw new RuntimeException(
                    "This notice has been cancelled."
            );
        }


        boolean alreadySubmitted =
                feedbackRepository
                        .existsByNotice_NoticeIdAndTenant_TenantId(
                                noticeId,
                                tenant.getTenantId()
                        );


        if (alreadySubmitted) {

            throw new RuntimeException(
                    "You have already submitted feedback for this notice."
            );
        }


        NoticeFeedback feedback =
                new NoticeFeedback();


        feedback.setNotice(notice);

        feedback.setTenant(tenant);

        feedback.setFeedback(
                request.getFeedback().trim()
        );

        feedback.setFeedbackDate(
                LocalDateTime.now()
        );


        feedbackRepository.save(feedback);


        return "Feedback submitted successfully.";
    }


    // ==================================================
    // GET FEEDBACK FOR ADMIN
    // ==================================================

    public List<NoticeFeedback> getNoticeFeedback(
            Long noticeId) {

        return feedbackRepository
                .findByNotice_NoticeIdOrderByFeedbackDateDesc(
                        noticeId
                );
    }


    // ==================================================
    // CANCEL NOTICE
    // ==================================================

    public NoticeResponse cancelNotice(
            Long noticeId) {

        Notice notice =
                noticeRepository.findById(noticeId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Notice not found."
                                )
                        );


        notice.setStatus("CANCELLED");


        notice =
                noticeRepository.save(notice);


        return convertToResponse(
                notice,
                false
        );
    }


    // ==================================================
    // STATUS
    // ==================================================

    private void updateStatus(
            Notice notice) {

        if ("CANCELLED".equalsIgnoreCase(
                notice.getStatus())) {

            return;
        }


        LocalDate today =
                LocalDate.now();


        if (notice.getEffectiveDate() == null) {

            notice.setStatus("ACTIVE");

            return;
        }


        if (today.isBefore(
                notice.getEffectiveDate())) {

            notice.setStatus("UPCOMING");

        } else {

            /*
             * Scheduled notice:
             * If today is the effective date and
             * toTime is already passed,
             * mark it completed.
             */

            if (notice.getToTime() != null &&
                    today.equals(
                            notice.getEffectiveDate()) &&
                    java.time.LocalTime.now().isAfter(
                            notice.getToTime())) {

                notice.setStatus("COMPLETED");

            } else {

                notice.setStatus("ACTIVE");
            }
        }


        noticeRepository.save(notice);
    }


    // ==================================================
    // VALIDATION
    // ==================================================

    private void validateRequest(
            NoticeRequest request) {

        if (request == null) {

            throw new RuntimeException(
                    "Notice data is required."
            );
        }


        if (request.getNoticeType() == null ||
                request.getNoticeType().trim().isEmpty()) {

            throw new RuntimeException(
                    "Notice type is required."
            );
        }


        if (request.getTitle() == null ||
                request.getTitle().trim().isEmpty()) {

            throw new RuntimeException(
                    "Notice title is required."
            );
        }


        if (request.getDescription() == null ||
                request.getDescription().trim().isEmpty()) {

            throw new RuntimeException(
                    "Description is required."
            );
        }


        if (request.getEffectiveDate() == null) {

            throw new RuntimeException(
                    "Effective date is required."
            );
        }


        if (request.getFromTime() != null &&
                request.getToTime() != null &&
                request.getToTime().isBefore(
                        request.getFromTime())) {

            throw new RuntimeException(
                    "To time cannot be before from time."
            );
        }
    }


    // ==================================================
    // GET TENANT
    // ==================================================

    private Tenant getTenant(
            String username) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found."
                                )
                        );


        return tenantRepository
                .findByUserId(user.getId())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Tenant details not found."
                        )
                );
    }


    // ==================================================
    // CONVERT
    // ==================================================

    private NoticeResponse convertToResponse(
            Notice notice,
            boolean feedbackSubmitted) {

        NoticeResponse response =
                new NoticeResponse();


        response.setNoticeId(
                notice.getNoticeId()
        );

        response.setNoticeType(
                notice.getNoticeType()
        );

        response.setTitle(
                notice.getTitle()
        );

        response.setDescription(
                notice.getDescription()
        );

        response.setPostedDate(
                notice.getPostedDate()
        );

        response.setEffectiveDate(
                notice.getEffectiveDate()
        );

        response.setFromTime(
                notice.getFromTime()
        );

        response.setToTime(
                notice.getToTime()
        );

        response.setFeedbackRequired(
                notice.isFeedbackRequired()
        );

        response.setFeedbackSubmitted(
                feedbackSubmitted
        );

        response.setStatus(
                notice.getStatus()
        );


        return response;
    }
}