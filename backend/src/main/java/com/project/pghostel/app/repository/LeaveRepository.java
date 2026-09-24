package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRepository
        extends JpaRepository<Leave, Long> {

    List<Leave> findByTenant_TenantIdOrderByFromDateAsc(
            Long tenantId
    );

    List<Leave> findAllByOrderByFromDateAsc();
}