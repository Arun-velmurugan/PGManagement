package com.project.pghostel.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.pghostel.app.entity.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByTenant_TenantId(Long tenantId);

    List<Complaint> findAllByOrderBySubmittedDateDesc();

    List<Complaint> findByTenant_TenantIdOrderBySubmittedDateDesc(Long tenantId);

}