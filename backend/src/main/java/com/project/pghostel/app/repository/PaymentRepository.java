package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByTenant_TenantIdAndMonth(
            Long tenantId,
            String month
    );

    List<Payment> findByTenant_TenantId(Long tenantId);

    boolean existsByTenant_TenantIdAndStatusIn(
            Long tenantId,
            List<String> statuses
    );
}