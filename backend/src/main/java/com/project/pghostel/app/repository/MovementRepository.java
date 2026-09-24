package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.Movement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovementRepository extends JpaRepository<Movement, Long> {

    Optional<Movement> findByTenant_TenantId(Long tenantId);
}