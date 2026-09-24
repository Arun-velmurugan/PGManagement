package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.TenantHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantHistoryRepository extends JpaRepository<TenantHistory, Long> {

    List<TenantHistory> findByTenantTenantIdOrderByHistoryIdAsc(Long tenantId);

}