package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository
        extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByPostedDateDesc();
}