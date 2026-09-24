package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.MovementHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovementHistoryRepository
        extends JpaRepository<MovementHistory, Long> {

    List<MovementHistory>
    findByTenant_TenantIdOrderByMovementTimeDesc(Long tenantId);
}