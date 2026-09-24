package com.project.pghostel.app.repository;

import com.project.pghostel.app.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {

    Optional<Food> findByTenant_TenantIdAndFoodDate(
            Long tenantId,
            LocalDate foodDate
    );

    boolean existsByTenant_TenantIdAndFoodDate(
            Long tenantId,
            LocalDate foodDate
    );

    List<Food> findByFoodDateOrderByFoodIdAsc(
            LocalDate foodDate
    );

    List<Food> findByTenant_TenantIdOrderByFoodDateDesc(
            Long tenantId
    );
}