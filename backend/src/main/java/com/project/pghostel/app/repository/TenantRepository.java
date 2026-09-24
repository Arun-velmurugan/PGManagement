package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    long countByRoomId(Long roomId);

     long countByRoomIdAndStatus(Long roomId, String status);

    boolean existsByUserId(Long userId);

    Optional<Tenant> findByUserId(Long userId);

    List<Tenant> findByStatus(String status);

    List<Tenant> findByRoomId(Long roomId);

    List<Tenant> findAllByOrderByTenantIdAsc();
}