package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.NoticeFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeFeedbackRepository
        extends JpaRepository<NoticeFeedback, Long> {

    Optional<NoticeFeedback>
    findByNotice_NoticeIdAndTenant_TenantId(
            Long noticeId,
            Long tenantId
    );


    List<NoticeFeedback>
    findByNotice_NoticeIdOrderByFeedbackDateDesc(
            Long noticeId
    );


    boolean existsByNotice_NoticeIdAndTenant_TenantId(
            Long noticeId,
            Long tenantId
    );
}